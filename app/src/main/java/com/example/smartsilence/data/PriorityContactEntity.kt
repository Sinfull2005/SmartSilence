package com.example.smartsilence.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "priority_contacts")
data class PriorityContactEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val phoneNumber: String,

    val enabled: Boolean = true
)