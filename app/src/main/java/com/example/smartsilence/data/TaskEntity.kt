package com.example.smartsilence.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String = "",

    val locationId: Int? = null,

    val reminderTime: Long? = null,

    val enabled: Boolean = true
)