package com.example.smartsilence.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.smartsilence.data.CalendarEvent

class CalendarDndScheduler(
    private val context: Context
) {

    companion object {

        private const val START_REQUEST_CODE = 2001
        private const val END_REQUEST_CODE = 2002

        private const val ACTION_CALENDAR_DND_START =
            "com.example.smartsilence.CALENDAR_DND_START"

        private const val ACTION_CALENDAR_DND_END =
            "com.example.smartsilence.CALENDAR_DND_END"
    }

    private val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    fun scheduleEvent(
        event: CalendarEvent
    ) {

        if (event.allDay) {
            return
        }

        if (event.endTime <= event.startTime) {
            return
        }

        scheduleAlarm(
            time = event.startTime,
            action = ACTION_CALENDAR_DND_START,
            requestCode =
                START_REQUEST_CODE + event.id.hashCode(),
            eventId = event.id
        )

        scheduleAlarm(
            time = event.endTime,
            action = ACTION_CALENDAR_DND_END,
            requestCode =
                END_REQUEST_CODE + event.id.hashCode(),
            eventId = event.id
        )
    }

    fun scheduleUpcomingEvents(
        events: List<CalendarEvent>
    ) {

        val now =
            System.currentTimeMillis()

        /*
         * Find the calendar events that are actually
         * running right now.
         */
        val currentlyRunningEventIds =
            events
                .filter { event ->
                    !event.allDay &&
                            event.startTime <= now &&
                            event.endTime > now
                }
                .map { event ->
                    event.id
                }
                .toSet()

        /*
         * Synchronize the stored state.
         *
         * This removes stale event IDs from previous
         * tests/events whose END alarm may have been missed.
         */
        CalendarDndState.syncActiveEvents(
            context,
            currentlyRunningEventIds
        )

        events.forEach { event ->

            if (event.allDay) {
                return@forEach
            }

            if (event.endTime <= now) {
                return@forEach
            }

            if (event.startTime <= now) {

                /*
                 * Event is already running.
                 *
                 * Its START alarm has already passed,
                 * so only schedule its END alarm.
                 */
                scheduleAlarm(
                    time = event.endTime,
                    action = ACTION_CALENDAR_DND_END,
                    requestCode =
                        END_REQUEST_CODE +
                                event.id.hashCode(),
                    eventId = event.id
                )

            } else {

                scheduleEvent(event)
            }
        }
    }

    private fun scheduleAlarm(
        time: Long,
        action: String,
        requestCode: Int,
        eventId: Long
    ) {

        if (time <= System.currentTimeMillis()) {
            return
        }

        val intent =
            Intent(
                context,
                CalendarDndReceiver::class.java
            ).apply {

                this.action = action

                putExtra(
                    "calendar_event_id",
                    eventId
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S
        ) {

            if (
                alarmManager.canScheduleExactAlarms()
            ) {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )

            } else {

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }
}

