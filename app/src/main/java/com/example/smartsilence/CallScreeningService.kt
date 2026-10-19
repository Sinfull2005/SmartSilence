package com.example.smartsilence

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

import com.example.smartsilence.data.SmartSilenceDatabase
import com.example.smartsilence.repository.SmartSilenceRepository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class SmartSilenceCallScreeningService :
    CallScreeningService() {

    companion object {

        private const val TAG =
            "SmartSilenceCallScreening"

        private const val PREFS_NAME =
            "smart_silence_geofence_state"

        private const val ACTIVE_IDS_KEY =
            "active_geofence_ids"
    }

    override fun onScreenCall(
        callDetails: Call.Details
    ) {

        val incomingNumber =
            callDetails.handle
                ?.schemeSpecificPart

        if (incomingNumber.isNullOrBlank()) {

            allowCall(callDetails)

            return
        }

        Log.d(
            TAG,
            "Incoming call: $incomingNumber"
        )

        val dndManager =
            DndManager(applicationContext)

        // =========================================================
        // DND OFF → ALLOW ALL CALLS
        // =========================================================

        if (!dndManager.isDndEnabled()) {

            Log.d(
                TAG,
                "Smart Silence inactive. Allowing call."
            )

            allowCall(callDetails)

            return
        }

        // =========================================================
        // DATABASE / REPOSITORY
        // =========================================================

        val database =
            SmartSilenceDatabase.getDatabase(
                applicationContext
            )

        val repository =
            SmartSilenceRepository(
                database.smartSilenceDao()
            )

        // =========================================================
        // GET ACTIVE LOCATION IDs
        // =========================================================

        val preferences =
            applicationContext.getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
            )

        val activeLocationIds =
            preferences
                .getStringSet(
                    ACTIVE_IDS_KEY,
                    emptySet()
                )
                ?.mapNotNull { id ->
                    id.toIntOrNull()
                }
                ?: emptyList()

        Log.d(
            TAG,
            "Active location IDs: $activeLocationIds"
        )

        // =========================================================
        // GET PRIORITY CONTACTS
        // =========================================================

        val priorityContacts =
            runBlocking {

                if (
                    activeLocationIds.isNotEmpty()
                ) {

                    // -------------------------------------------------
                    // LOCATION IS ACTIVE
                    // Use ONLY contacts assigned to active locations.
                    // -------------------------------------------------

                    activeLocationIds
                        .flatMap { locationId ->

                            repository
                                .getPriorityContactsForLocation(
                                    locationId
                                )
                        }
                        .distinctBy {
                            it.id
                        }

                } else {

                    // -------------------------------------------------
                    // NO LOCATION IS ACTIVE
                    // Preserve global priority contacts.
                    // -------------------------------------------------

                    repository
                        .allPriorityContacts
                        .first()
                }
            }

        Log.d(
            TAG,
            "Priority contacts: " +
                    priorityContacts.map {
                        it.name
                    }
        )

        // =========================================================
        // CHECK INCOMING NUMBER
        // =========================================================

        val normalizedIncoming =
            normalizePhoneNumber(
                incomingNumber
            )

        val isPriorityContact =
            priorityContacts.any { contact ->

                contact.enabled &&

                        normalizePhoneNumber(
                            contact.phoneNumber
                        ) == normalizedIncoming
            }

        // =========================================================
        // BUILD CALL RESPONSE
        // =========================================================

        val response =
            CallScreeningService
                .CallResponse
                .Builder()

        if (isPriorityContact) {

            // -----------------------------------------------------
            // PRIORITY CONTACT
            // Allow ringtone.
            // NO automatic SMS.
            // -----------------------------------------------------

            Log.d(
                TAG,
                "Priority contact detected. " +
                        "Allowing ringtone."
            )

            response
                .setDisallowCall(false)
                .setRejectCall(false)

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                response.setSilenceCall(false)
            }

        } else {

            // -----------------------------------------------------
            // NON-PRIORITY CONTACT
            // Keep call but silence ringtone.
            // -----------------------------------------------------

            Log.d(
                TAG,
                "Non-priority call detected. " +
                        "Silencing ringtone."
            )

            response
                .setDisallowCall(false)
                .setRejectCall(false)

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                response.setSilenceCall(true)
            }

            // -----------------------------------------------------
            // CUSTOM AUTOMATIC SMS REPLY
            // -----------------------------------------------------

            sendCustomAutoReply(
                phoneNumber = incomingNumber,
                repository = repository
            )
        }

        respondToCall(
            callDetails,
            response.build()
        )
    }

    // =============================================================
    // CUSTOM AUTO REPLY
    // =============================================================

    private fun sendCustomAutoReply(
        phoneNumber: String,
        repository: SmartSilenceRepository
    ) {

        // ---------------------------------------------------------
        // CHECK SMS PERMISSION
        // ---------------------------------------------------------

        if (
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.w(
                TAG,
                "SEND_SMS permission is not granted. " +
                        "Auto reply skipped."
            )

            return
        }

        // ---------------------------------------------------------
        // GET SAVED SETTINGS
        // ---------------------------------------------------------

        val settings =
            runBlocking {

                repository.settings.first()
            }

        if (settings == null) {

            Log.w(
                TAG,
                "No Smart Silence settings found. " +
                        "Auto reply skipped."
            )

            return
        }

        // ---------------------------------------------------------
        // AUTO REPLY DISABLED
        // ---------------------------------------------------------

        if (!settings.autoReplyEnabled) {

            Log.d(
                TAG,
                "Automatic SMS is disabled."
            )

            return
        }

        // ---------------------------------------------------------
        // GET CUSTOM MESSAGE
        // ---------------------------------------------------------

        val message =
            settings.autoReplyMessage
                .trim()

        if (message.isEmpty()) {

            Log.w(
                TAG,
                "Automatic SMS message is empty. " +
                        "Auto reply skipped."
            )

            return
        }

        // ---------------------------------------------------------
        // SEND SMS
        // ---------------------------------------------------------

        try {

            val smsManager =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {

                    applicationContext.getSystemService(
                        SmsManager::class.java
                    )

                } else {

                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

            val parts =
                smsManager.divideMessage(
                    message
                )

            if (parts.size == 1) {

                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )

            } else {

                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            }

            Log.d(
                TAG,
                "Custom automatic SMS sent successfully."
            )

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to send automatic SMS.",
                exception
            )
        }
    }

    // =============================================================
    // ALLOW CALL
    // =============================================================

    private fun allowCall(
        callDetails: Call.Details
    ) {

        val response =
            CallScreeningService
                .CallResponse
                .Builder()
                .setDisallowCall(false)
                .setRejectCall(false)

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {

            response.setSilenceCall(false)
        }

        respondToCall(
            callDetails,
            response.build()
        )
    }

    // =============================================================
    // NORMALIZE PHONE NUMBER
    // =============================================================

    private fun normalizePhoneNumber(
        number: String
    ): String {

        return number
            .replace(
                Regex("[^0-9]"),
                ""
            )
            .takeLast(10)
    }
}