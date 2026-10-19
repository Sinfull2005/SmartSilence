package com.example.smartsilence.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "location_priority_contacts",

    primaryKeys = [
        "locationId",
        "contactId"
    ],

    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = PriorityContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index("locationId"),
        Index("contactId")
    ]
)
data class LocationPriorityContactEntity(

    val locationId: Int,

    val contactId: Int
)