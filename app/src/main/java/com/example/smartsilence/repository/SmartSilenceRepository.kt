package com.example.smartsilence.repository

import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.data.LocationPriorityContactEntity
import com.example.smartsilence.data.PriorityContactEntity
import com.example.smartsilence.data.SettingsEntity
import com.example.smartsilence.data.SmartSilenceDao
import com.example.smartsilence.data.TaskEntity

import kotlinx.coroutines.flow.Flow

class SmartSilenceRepository(
    private val dao: SmartSilenceDao
) {

    // ============================================================
    // LOCATIONS
    // ============================================================

    val allLocations:
            Flow<List<LocationEntity>> =
        dao.getAllLocations()

    // Kept for compatibility with existing code.
    val locations:
            Flow<List<LocationEntity>> =
        allLocations

    suspend fun insertLocation(
        location: LocationEntity
    ) {
        dao.insertLocation(
            location
        )
    }

    suspend fun updateLocation(
        location: LocationEntity
    ) {
        dao.updateLocation(
            location
        )
    }

    suspend fun deleteLocation(
        location: LocationEntity
    ) {
        dao.deleteLocation(
            location
        )
    }


    // ============================================================
    // TASKS
    // ============================================================

    val allTasks:
            Flow<List<TaskEntity>> =
        dao.getAllTasks()

    // Kept for compatibility with existing code.
    val tasks:
            Flow<List<TaskEntity>> =
        allTasks

    suspend fun insertTask(
        task: TaskEntity
    ) {
        dao.insertTask(
            task
        )
    }

    suspend fun updateTask(
        task: TaskEntity
    ) {
        dao.updateTask(
            task
        )
    }

    suspend fun deleteTask(
        task: TaskEntity
    ) {
        dao.deleteTask(
            task
        )
    }

    suspend fun getEnabledTasksForLocation(
        locationId: Int
    ): List<TaskEntity> {

        return dao.getEnabledTasksForLocation(
            locationId
        )
    }


    // ============================================================
    // PRIORITY CONTACTS
    // ============================================================

    val allPriorityContacts:
            Flow<List<PriorityContactEntity>> =
        dao.getAllPriorityContacts()

    // Kept for compatibility with existing code.
    val priorityContacts:
            Flow<List<PriorityContactEntity>> =
        allPriorityContacts

    suspend fun insertPriorityContact(
        contact: PriorityContactEntity
    ) {
        dao.insertPriorityContact(
            contact
        )
    }

    suspend fun deletePriorityContact(
        contact: PriorityContactEntity
    ) {
        dao.deletePriorityContact(
            contact
        )
    }

    // ------------------------------------------------------------
    // DELETE ALL PRIORITY CONTACTS
    // ------------------------------------------------------------

    suspend fun deleteAllPriorityContacts() {

        dao.deleteAllPriorityContacts()
    }


    // ============================================================
    // LOCATION ↔ PRIORITY CONTACTS
    // ============================================================

    suspend fun assignPriorityContactToLocation(
        locationId: Int,
        contactId: Int
    ) {

        dao.assignPriorityContactToLocation(

            LocationPriorityContactEntity(
                locationId =
                    locationId,

                contactId =
                    contactId
            )
        )
    }

    // Kept for compatibility with older code.
    suspend fun addPriorityContactToLocation(
        locationId: Int,
        contactId: Int
    ) {

        assignPriorityContactToLocation(
            locationId =
                locationId,

            contactId =
                contactId
        )
    }

    suspend fun removePriorityContactFromLocation(
        locationId: Int,
        contactId: Int
    ) {

        dao.removePriorityContactFromLocation(

            LocationPriorityContactEntity(
                locationId =
                    locationId,

                contactId =
                    contactId
            )
        )
    }

    suspend fun getPriorityContactsForLocation(
        locationId: Int
    ): List<PriorityContactEntity> {

        return dao.getPriorityContactsForLocation(
            locationId
        )
    }

    suspend fun getContactRelationsForLocation(
        locationId: Int
    ): List<LocationPriorityContactEntity> {

        return dao.getContactRelationsForLocation(
            locationId
        )
    }


    // ============================================================
    // SETTINGS
    // ============================================================

    val settings:
            Flow<SettingsEntity?> =
        dao.getSettings()

    suspend fun saveSettings(
        settings: SettingsEntity
    ) {

        dao.saveSettings(
            settings
        )
    }
}