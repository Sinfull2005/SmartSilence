package com.example.smartsilence.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log

import androidx.core.content.edit

import com.example.smartsilence.DndManager
import com.example.smartsilence.calendar.CalendarDndState
import com.example.smartsilence.data.SmartSilenceDatabase
import com.example.smartsilence.notification.TaskNotificationManager
import com.example.smartsilence.security.SuspiciousMovementDetector

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

import java.util.Locale

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmartSilenceGeofence"

        private const val PREFS_NAME =
            "smart_silence_geofence_state"

        private const val ACTIVE_IDS_KEY =
            "active_geofence_ids"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(
                TAG,
                "Geofence error: ${geofencingEvent.errorCode}"
            )
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeredGeofences = geofencingEvent.triggeringGeofences

        if (triggeredGeofences.isNullOrEmpty()) {
            Log.w(TAG, "No triggering geofences found")
            return
        }

        val appContext = context.applicationContext
        val dndManager = DndManager(appContext)

        val preferences = appContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val activeIds = preferences
            .getStringSet(ACTIVE_IDS_KEY, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()

        val movementDetector = SuspiciousMovementDetector(appContext)

        // ====================================================
        // ENTER
        // ====================================================

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "ENTER detected")

            triggeredGeofences.forEach { geofence ->
                val locationId = geofence.requestId

                activeIds.add(locationId)

                Log.d(TAG, "Entered geofence: $locationId")

                // Movement detection is retained for logging only.
                val suspicious = movementDetector.recordEnter(locationId)

                if (suspicious) {
                    Log.w(
                        TAG,
                        "Suspicious movement detected for location: $locationId"
                    )
                }

                // Preserve location-based task reminders.
                showLocationTasks(
                    context = appContext,
                    locationId = locationId.toIntOrNull()
                )
            }

            preferences.edit {
                putStringSet(ACTIVE_IDS_KEY, activeIds)
            }

            // Saved-location DND is deterministic for now.
            // No context-aware risk assessment or trust prompt.
            if (activeIds.isNotEmpty()) {
                if (dndManager.hasDndAccess()) {
                    dndManager.enableDnd()

                    Log.d(
                        TAG,
                        "DND enabled on saved-location entry."
                    )

                    speakAcknowledgement(
                        appContext,
                        "Smart Silence enabled. Do Not Disturb is now active."
                    )
                } else {
                    Log.w(
                        TAG,
                        "Cannot enable DND: access is not granted."
                    )
                }
            }

            return
        }

        // ====================================================
        // EXIT
        // ====================================================

        if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "EXIT detected")

            triggeredGeofences.forEach { geofence ->
                val locationId = geofence.requestId

                activeIds.remove(locationId)

                Log.d(TAG, "Exited geofence: $locationId")

                // Movement detection is retained for logging only.
                val suspicious = movementDetector.recordExit(locationId)

                if (suspicious) {
                    Log.w(
                        TAG,
                        "Suspicious rapid exit detected for location: $locationId"
                    )
                }
            }

            preferences.edit {
                putStringSet(ACTIVE_IDS_KEY, activeIds)
            }

            // Preserve calendar-based DND.
            val calendarEventActive =
                CalendarDndState.hasActiveCalendarEvent(appContext)

            // Turn DND off only when no saved location or calendar
            // event is keeping it active.
            if (
                activeIds.isEmpty() &&
                !calendarEventActive &&
                dndManager.hasDndAccess()
            ) {
                dndManager.disableDnd()

                Log.d(TAG, "DND disabled automatically.")

                speakAcknowledgement(
                    appContext,
                    "Smart Silence disabled. Do Not Disturb is now inactive."
                )
            } else {
                Log.d(
                    TAG,
                    "DND remains active. " +
                            "Active locations=${activeIds.size}, " +
                            "calendarEventActive=$calendarEventActive"
                )
            }

            return
        }

        Log.d(
            TAG,
            "Unhandled geofence transition: $transition"
        )
    }

    // ========================================================
    // LOCATION TASK REMINDERS
    // ========================================================

    private fun showLocationTasks(
        context: Context,
        locationId: Int?
    ) {
        if (locationId == null) {
            Log.w(
                TAG,
                "Could not determine location ID from geofence request ID"
            )
            return
        }

        val pendingResult = goAsync()

        val receiverScope = CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

        receiverScope.launch {
            try {
                val database = SmartSilenceDatabase.getDatabase(
                    context.applicationContext
                )

                val tasks = database
                    .smartSilenceDao()
                    .getEnabledTasksForLocation(locationId)

                Log.d(
                    TAG,
                    "Found ${tasks.size} enabled task(s) for location $locationId"
                )

                if (tasks.isEmpty()) {
                    return@launch
                }

                val notificationManager = TaskNotificationManager(
                    context.applicationContext
                )

                notificationManager.createNotificationChannel()

                tasks.forEach { task ->
                    Log.d(
                        TAG,
                        "Showing task notification: ${task.title}"
                    )

                    notificationManager.showTaskNotification(task)
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Failed to show location task reminders",
                    exception
                )
            } finally {
                pendingResult.finish()
                receiverScope.cancel()
            }
        }
    }

    // ========================================================
    // VOICE ACKNOWLEDGEMENT
    // ========================================================

    private fun speakAcknowledgement(
        context: Context,
        message: String
    ) {
        // Declare the engine before registering the callback so the
        // callback can safely refer to it after initialization.
        lateinit var engine: TextToSpeech

        engine = TextToSpeech(
            context.applicationContext
        ) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e(TAG, "TTS initialization failed")
                return@TextToSpeech
            }

            val result = engine.setLanguage(Locale.getDefault())

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e(TAG, "Default language not supported by TTS")
                engine.shutdown()
                return@TextToSpeech
            }

            engine.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "SmartSilenceGeofence"
            )

            Log.d(TAG, "Voice acknowledgement requested")
        }
    }
}
