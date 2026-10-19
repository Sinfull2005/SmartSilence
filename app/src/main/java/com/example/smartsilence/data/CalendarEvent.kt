package com.example.smartsilence.data

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val location: String,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val calendarName: String
)