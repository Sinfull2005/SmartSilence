package com.example.smartsilence

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smartsilence.calendar.CalendarDndScheduler
import com.example.smartsilence.calendar.CalendarInfo
import com.example.smartsilence.calendar.CalendarManager
import com.example.smartsilence.data.CalendarEvent
import com.example.smartsilence.ui.theme.TextSecondary
import com.example.smartsilence.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val calendarManager =
        remember {
            CalendarManager(context)
        }

    var hasPermission by remember {
        mutableStateOf(
            calendarManager.hasCalendarPermission()
        )
    }

    var calendars by remember {
        mutableStateOf<List<CalendarInfo>>(
            emptyList()
        )
    }

    var selectedCalendarIds by remember {
        mutableStateOf<Set<Long>>(
            emptySet()
        )
    }

    var events by remember {
        mutableStateOf<List<CalendarEvent>>(
            emptyList()
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasPermission = granted

            if (granted) {

                calendars =
                    calendarManager.getCalendars()

                selectedCalendarIds =
                    calendarManager
                        .getSelectedCalendarIds()

                events =
                    calendarManager
                        .getUpcomingEvents()

                CalendarDndScheduler(context)
                    .scheduleUpcomingEvents(events)
            }
        }

    LaunchedEffect(hasPermission) {

        if (hasPermission) {

            calendars =
                calendarManager.getCalendars()

            selectedCalendarIds =
                calendarManager
                    .getSelectedCalendarIds()

            events =
                calendarManager
                    .getUpcomingEvents()

            CalendarDndScheduler(context)
                .scheduleUpcomingEvents(events)
        }
    }

    if (!hasPermission) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Calendar Sync",
                    color = TextWhite,
                    style =
                        MaterialTheme.typography.headlineSmall
                )

                Button(
                    onClick = onBack
                ) {
                    Text(
                        text = "Back",
                        color = TextWhite
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text =
                    "Calendar permission is required " +
                            "to read your events.",
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {

                    permissionLauncher.launch(
                        Manifest.permission.READ_CALENDAR
                    )
                }
            ) {

                Text(
                    text = "Allow Calendar Access",
                    color = TextWhite
                )
            }
        }

    } else {

        /*
         * ONE LazyColumn controls the entire screen.
         *
         * This fixes the scrolling problem because:
         * - Calendar list scrolls
         * - Upcoming Events scroll
         * - Header and other content scroll together
         *
         * There is no LazyColumn nested inside another
         * scrolling container.
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "Calendar Sync",
                        color = TextWhite,
                        style =
                            MaterialTheme.typography.headlineSmall
                    )

                    Button(
                        onClick = onBack
                    ) {

                        Text(
                            text = "Back",
                            color = TextWhite
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Calendars",
                    color = TextWhite,
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            if (calendars.isEmpty()) {

                item {

                    Text(
                        text =
                            "No calendars found on this device.",
                        color = TextSecondary
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }

            } else {

                items(
                    items = calendars,
                    key = { calendar ->
                        calendar.id
                    }
                ) { calendar ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked =
                                selectedCalendarIds
                                    .contains(calendar.id),

                            onCheckedChange = { checked ->

                                val updated =
                                    selectedCalendarIds
                                        .toMutableSet()

                                if (checked) {
                                    updated.add(calendar.id)
                                } else {
                                    updated.remove(calendar.id)
                                }

                                selectedCalendarIds =
                                    updated

                                calendarManager
                                    .saveSelectedCalendarIds(
                                        updated
                                    )

                                /*
                                 * Reload events after the
                                 * calendar selection changes.
                                 */
                                events =
                                    calendarManager
                                        .getUpcomingEvents()

                                /*
                                 * Reschedule DND for the
                                 * newly selected calendars.
                                 */
                                CalendarDndScheduler(context)
                                    .scheduleUpcomingEvents(
                                        events
                                    )
                            }
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text = calendar.name,
                                color = TextWhite
                            )

                            if (
                                calendar.accountName
                                    .isNotBlank()
                            ) {

                                Text(
                                    text =
                                        calendar.accountName,
                                    color =
                                        TextSecondary,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                )
                            }
                        }
                    }
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Upcoming Events",
                    color = TextWhite,
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                if (selectedCalendarIds.isEmpty()) {

                    Text(
                        text =
                            "Select at least one calendar " +
                                    "to see its events.",
                        color = TextSecondary
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                } else if (events.isEmpty()) {

                    Text(
                        text = "No upcoming events found.",
                        color = TextSecondary
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }
            }

            if (
                selectedCalendarIds.isNotEmpty() &&
                events.isNotEmpty()
            ) {

                items(
                    items = events,
                    key = { event ->
                        event.id
                    }
                ) { event ->

                    CalendarEventItem(
                        event = event
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarEventItem(
    event: CalendarEvent
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = event.title,
            color = TextWhite,
            style =
                MaterialTheme.typography.titleMedium
        )

        Text(
            text = event.calendarName,
            color = TextSecondary,
            style =
                MaterialTheme.typography.bodySmall
        )

        Text(
            text =
                formatEventTime(
                    event.startTime,
                    event.endTime
                ),
            color = TextSecondary,
            style =
                MaterialTheme.typography.bodySmall
        )

        if (event.location.isNotBlank()) {

            Text(
                text = event.location,
                color = TextSecondary,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatEventTime(
    startTime: Long,
    endTime: Long
): String {

    val formatter =
        SimpleDateFormat(
            "dd MMM, hh:mm a",
            Locale.getDefault()
        )

    return "${formatter.format(Date(startTime))} - " +
            formatter.format(Date(endTime))
}

