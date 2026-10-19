package com.example.smartsilence.calendar

import android.content.Context

object CalendarDndState {

    private const val PREFS_NAME =
        "smart_silence_calendar_dnd_state"

    private const val ACTIVE_EVENT_IDS =
        "active_calendar_event_ids"

    private const val GEOFENCE_PREFS_NAME =
        "smart_silence_geofence_state"

    private const val ACTIVE_GEOFENCE_IDS =
        "active_geofence_ids"

    private fun preferences(
        context: Context
    ) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun getActiveEventIds(
        context: Context
    ): Set<Long> {

        return preferences(context)
            .getStringSet(
                ACTIVE_EVENT_IDS,
                emptySet()
            )
            ?.mapNotNull {
                it.toLongOrNull()
            }
            ?.toSet()
            ?: emptySet()
    }

    fun addActiveEvent(
        context: Context,
        eventId: Long
    ) {

        val updated =
            getActiveEventIds(context)
                .toMutableSet()

        updated.add(eventId)

        preferences(context)
            .edit()
            .putStringSet(
                ACTIVE_EVENT_IDS,
                updated.map { it.toString() }.toSet()
            )
            .apply()
    }

    fun removeActiveEvent(
        context: Context,
        eventId: Long
    ) {

        val updated =
            getActiveEventIds(context)
                .toMutableSet()

        updated.remove(eventId)

        preferences(context)
            .edit()
            .putStringSet(
                ACTIVE_EVENT_IDS,
                updated.map { it.toString() }.toSet()
            )
            .apply()
    }

    /*
     * Synchronize the stored active events with the events
     * that are actually running right now.
     *
     * This removes stale event IDs left behind if an older
     * calendar END alarm was missed.
     */
    fun syncActiveEvents(
        context: Context,
        activeEventIds: Set<Long>
    ) {

        preferences(context)
            .edit()
            .putStringSet(
                ACTIVE_EVENT_IDS,
                activeEventIds
                    .map { it.toString() }
                    .toSet()
            )
            .apply()
    }

    fun hasActiveCalendarEvent(
        context: Context
    ): Boolean {

        return getActiveEventIds(context)
            .isNotEmpty()
    }

    fun hasActiveLocation(
        context: Context
    ): Boolean {

        val preferences =
            context.getSharedPreferences(
                GEOFENCE_PREFS_NAME,
                Context.MODE_PRIVATE
            )

        return preferences
            .getStringSet(
                ACTIVE_GEOFENCE_IDS,
                emptySet()
            )
            ?.isNotEmpty() == true
    }
}

