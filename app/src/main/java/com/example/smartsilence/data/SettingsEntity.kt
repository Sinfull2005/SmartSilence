package com.example.smartsilence.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    val smartSilenceEnabled: Boolean = false,

    val autoReplyEnabled: Boolean = false,

    val autoReplyMessage: String =
        "Hi, I'm currently unavailable. I'll get back to you soon.",

    val batteryOptimizationEnabled: Boolean = true
)