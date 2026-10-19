package com.example.smartsilence.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow

@Dao
interface SmartSilenceDao {

    // ============================================================
    // LOCATIONS
    // ============================================================

    @Query(
        "SELECT * FROM locations ORDER BY id DESC"
    )
    fun getAllLocations():
            Flow<List<LocationEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertLocation(
        location: LocationEntity
    )

    @Update
    suspend fun updateLocation(
        location: LocationEntity
    )

    @Delete
    suspend fun deleteLocation(
        location: LocationEntity
    )


    // ============================================================
    // TASKS
    // ============================================================

    @Query(
        "SELECT * FROM tasks ORDER BY id DESC"
    )
    fun getAllTasks():
            Flow<List<TaskEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertTask(
        task: TaskEntity
    )

    @Update
    suspend fun updateTask(
        task: TaskEntity
    )

    @Delete
    suspend fun deleteTask(
        task: TaskEntity
    )

    @Query(
        """
        SELECT *
        FROM tasks
        WHERE locationId = :locationId
          AND enabled = 1
        ORDER BY id DESC
        """
    )
    suspend fun getEnabledTasksForLocation(
        locationId: Int
    ): List<TaskEntity>


    // ============================================================
    // PRIORITY CONTACTS
    // ============================================================

    @Query(
        "SELECT * FROM priority_contacts ORDER BY id DESC"
    )
    fun getAllPriorityContacts():
            Flow<List<PriorityContactEntity>>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertPriorityContact(
        contact: PriorityContactEntity
    )

    @Delete
    suspend fun deletePriorityContact(
        contact: PriorityContactEntity
    )

    // ------------------------------------------------------------
    // DELETE ALL PRIORITY CONTACTS
    // ------------------------------------------------------------

    @Query(
        "DELETE FROM priority_contacts"
    )
    suspend fun deleteAllPriorityContacts()


    // ============================================================
    // LOCATION ↔ PRIORITY CONTACTS
    // ============================================================

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun assignPriorityContactToLocation(
        relation: LocationPriorityContactEntity
    )

    @Delete
    suspend fun removePriorityContactFromLocation(
        relation: LocationPriorityContactEntity
    )

    @Query(
        """
        SELECT pc.*
        FROM priority_contacts pc
        INNER JOIN location_priority_contacts lpc
            ON pc.id = lpc.contactId
        WHERE lpc.locationId = :locationId
        ORDER BY pc.id DESC
        """
    )
    suspend fun getPriorityContactsForLocation(
        locationId: Int
    ): List<PriorityContactEntity>

    @Query(
        """
        SELECT *
        FROM location_priority_contacts
        WHERE locationId = :locationId
        """
    )
    suspend fun getContactRelationsForLocation(
        locationId: Int
    ): List<LocationPriorityContactEntity>


    // ============================================================
    // SETTINGS
    // ============================================================

    @Query(
        "SELECT * FROM settings LIMIT 1"
    )
    fun getSettings():
            Flow<SettingsEntity?>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun saveSettings(
        settings: SettingsEntity
    )
}