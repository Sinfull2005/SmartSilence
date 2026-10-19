package com.example.smartsilence.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val latitude: Double,

    val longitude: Double,

    val radius: Float = 100f,

    val dndEnabled: Boolean = true
)