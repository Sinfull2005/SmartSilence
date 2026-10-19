package com.example.smartsilence.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.smartsilence.data.CalendarEvent

data class CalendarInfo(
    val id: Long,
    val name: String,
    val accountName: String
)

class CalendarManager(
    private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "smart_silence_calendar_prefs"
        private const val SELECTED_CALENDAR_IDS = "selected_calendar_ids"
    }

    private val preferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns all calendars available on the phone.
     */
    fun getCalendars(): List<CalendarInfo> {

        if (!hasCalendarPermission()) {
            return emptyList()
        }

        val calendars = mutableListOf<CalendarInfo>()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " ASC"
        )?.use { cursor ->

            val idIndex =
                cursor.getColumnIndex(CalendarContract.Calendars._ID)

            val nameIndex =
                cursor.getColumnIndex(
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
                )

            val accountIndex =
                cursor.getColumnIndex(
                    CalendarContract.Calendars.ACCOUNT_NAME
                )

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idIndex)

                calendars.add(
                    CalendarInfo(
                        id = id,
                        name = cursor.getString(nameIndex)
                            ?: "Calendar",
                        accountName = cursor.getString(accountIndex)
                            ?: ""
                    )
                )
            }
        }

        return calendars
    }

    /**
     * Returns the IDs of calendars selected by the user.
     */
    fun getSelectedCalendarIds(): Set<Long> {

        return preferences
            .getStringSet(
                SELECTED_CALENDAR_IDS,
                emptySet()
            )
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    /**
     * Saves the calendars selected by the user.
     */
    fun saveSelectedCalendarIds(
        calendarIds: Set<Long>
    ) {

        preferences.edit()
            .putStringSet(
                SELECTED_CALENDAR_IDS,
                calendarIds.map { it.toString() }.toSet()
            )
            .apply()
    }

    /**
     * Returns upcoming events only from the calendars
     * selected by the user.
     */
    fun getUpcomingEvents(): List<CalendarEvent> {

        if (!hasCalendarPermission()) {
            return emptyList()
        }

        val selectedCalendarIds =
            getSelectedCalendarIds()

        /*
         * If the user has not selected any calendars,
         * don't schedule/use any calendar events.
         */
        if (selectedCalendarIds.isEmpty()) {
            return emptyList()
        }

        val events = mutableListOf<CalendarEvent>()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME
        )

        val now = System.currentTimeMillis()

        val endTime =
            now + (30L * 24L * 60L * 60L * 1000L)

        val uri =
            CalendarContract.Instances.CONTENT_URI
                .buildUpon()
                .appendPath(now.toString())
                .appendPath(endTime.toString())
                .build()

        /*
         * Create:
         *
         * CALENDAR_ID IN (?, ?, ?)
         *
         * depending on how many calendars the user selected.
         */
        val placeholders =
            selectedCalendarIds.joinToString(",") {
                "?"
            }

        val selection =
            CalendarContract.Instances.CALENDAR_ID +
                    " IN ($placeholders)"

        val selectionArgs =
            selectedCalendarIds
                .map { it.toString() }
                .toTypedArray()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            CalendarContract.Instances.BEGIN + " ASC"
        )?.use { cursor ->

            val eventIdIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.EVENT_ID
                )

            val titleIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.TITLE
                )

            val descriptionIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.DESCRIPTION
                )

            val locationIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.EVENT_LOCATION
                )

            val beginIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.BEGIN
                )

            val endIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.END
                )

            val allDayIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.ALL_DAY
                )

            val calendarNameIndex =
                cursor.getColumnIndex(
                    CalendarContract.Instances.CALENDAR_DISPLAY_NAME
                )

            while (cursor.moveToNext()) {

                events.add(
                    CalendarEvent(
                        id = cursor.getLong(eventIdIndex),
                        title = cursor.getString(titleIndex)
                            ?: "Untitled event",
                        description =
                            cursor.getString(descriptionIndex)
                                ?: "",
                        location =
                            cursor.getString(locationIndex)
                                ?: "",
                        startTime =
                            cursor.getLong(beginIndex),
                        endTime =
                            cursor.getLong(endIndex),
                        allDay =
                            cursor.getInt(allDayIndex) != 0,
                        calendarName =
                            cursor.getString(calendarNameIndex)
                                ?: "Calendar"
                    )
                )
            }
        }

        return events
    }
}