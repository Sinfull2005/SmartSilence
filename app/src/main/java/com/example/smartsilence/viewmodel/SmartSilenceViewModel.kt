package com.example.smartsilence.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.data.PriorityContactEntity
import com.example.smartsilence.data.SettingsEntity
import com.example.smartsilence.data.TaskEntity
import com.example.smartsilence.repository.SmartSilenceRepository

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
class SmartSilenceViewModel(
    private val repository: SmartSilenceRepository
) : ViewModel() {

    // ============================================================
    // LOCATIONS
    // ============================================================

    val locations:
            StateFlow<List<LocationEntity>> =
        repository.allLocations.stateIn(

            scope =
                viewModelScope,

            started =
                SharingStarted.WhileSubscribed(5000),

            initialValue =
                emptyList()
        )

    // True after Room emits the saved locations from the database.
    val locationsLoaded: StateFlow<Boolean> =
        repository.allLocations
            .map { true }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )



    // ============================================================
    // ADD LOCATION
    // ============================================================

    fun addLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Float
    ) {

        val location =
            LocationEntity(

                name =
                    name,

                latitude =
                    latitude,

                longitude =
                    longitude,

                radius =
                    radius,

                dndEnabled =
                    true
            )

        viewModelScope.launch {

            repository.insertLocation(
                location
            )
        }
    }


    // ============================================================
    // UPDATE LOCATION
    // ============================================================

    fun updateLocation(
        location: LocationEntity
    ) {

        viewModelScope.launch {

            repository.updateLocation(
                location
            )
        }
    }


    // ============================================================
    // DELETE LOCATION
    // ============================================================

    fun deleteLocation(
        location: LocationEntity
    ) {

        viewModelScope.launch {

            repository.deleteLocation(
                location
            )
        }
    }


    // ============================================================
    // TASKS
    // ============================================================

    val tasks:
            StateFlow<List<TaskEntity>> =
        repository.allTasks.stateIn(

            scope =
                viewModelScope,

            started =
                SharingStarted.WhileSubscribed(5000),

            initialValue =
                emptyList()
        )


    // ============================================================
    // ADD TASK
    // ============================================================

    fun addTask(
        title: String,
        description: String,
        locationId: Int?
    ) {

        val task =
            TaskEntity(

                title =
                    title,

                description =
                    description,

                locationId =
                    locationId,

                enabled =
                    true
            )

        viewModelScope.launch {

            repository.insertTask(
                task
            )
        }
    }


    // ============================================================
    // UPDATE TASK
    // ============================================================

    fun updateTask(
        task: TaskEntity
    ) {

        viewModelScope.launch {

            repository.updateTask(
                task
            )
        }
    }


    // ============================================================
    // DELETE TASK
    // ============================================================

    fun deleteTask(
        task: TaskEntity
    ) {

        viewModelScope.launch {

            repository.deleteTask(
                task
            )
        }
    }


    // ============================================================
    // GET TASKS FOR LOCATION
    // ============================================================

    suspend fun getEnabledTasksForLocation(
        locationId: Int
    ): List<TaskEntity> {

        return repository.getEnabledTasksForLocation(
            locationId
        )
    }


    // ============================================================
    // PRIORITY CONTACTS
    // ============================================================

    val priorityContacts:
            StateFlow<List<PriorityContactEntity>> =
        repository.allPriorityContacts.stateIn(

            scope =
                viewModelScope,

            started =
                SharingStarted.WhileSubscribed(5000),

            initialValue =
                emptyList()
        )


    // ============================================================
    // ADD PRIORITY CONTACT
    // ============================================================

    fun addPriorityContact(
        name: String,
        phoneNumber: String
    ) {

        val contact =
            PriorityContactEntity(

                name =
                    name,

                phoneNumber =
                    phoneNumber,

                enabled =
                    true
            )

        viewModelScope.launch {

            repository.insertPriorityContact(
                contact
            )
        }
    }


    // ============================================================
    // DELETE PRIORITY CONTACT
    // ============================================================

    fun deletePriorityContact(
        contact: PriorityContactEntity
    ) {

        viewModelScope.launch {

            repository.deletePriorityContact(
                contact
            )
        }
    }


    // ============================================================
    // DELETE ALL PRIORITY CONTACTS
    // ============================================================

    fun deleteAllPriorityContacts() {

        viewModelScope.launch {

            repository.deleteAllPriorityContacts()
        }
    }


    // ============================================================
    // LOCATION ↔ PRIORITY CONTACTS
    // ============================================================

    fun assignPriorityContactToLocation(
        locationId: Int,
        contactId: Int
    ) {

        viewModelScope.launch {

            repository.assignPriorityContactToLocation(

                locationId =
                    locationId,

                contactId =
                    contactId
            )
        }
    }


    fun removePriorityContactFromLocation(
        locationId: Int,
        contactId: Int
    ) {

        viewModelScope.launch {

            repository.removePriorityContactFromLocation(

                locationId =
                    locationId,

                contactId =
                    contactId
            )
        }
    }


    suspend fun getPriorityContactsForLocation(
        locationId: Int
    ): List<PriorityContactEntity> {

        return repository.getPriorityContactsForLocation(
            locationId
        )
    }


    suspend fun getContactRelationsForLocation(
        locationId: Int
    ): List<com.example.smartsilence.data.LocationPriorityContactEntity> {

        return repository.getContactRelationsForLocation(
            locationId
        )
    }


    // ============================================================
    // SETTINGS
    // ============================================================

    val settings:
            StateFlow<SettingsEntity?> =
        repository.settings.stateIn(

            scope =
                viewModelScope,

            started =
                SharingStarted.WhileSubscribed(5000),

            initialValue =
                null
        )


    fun saveSettings(
        settings: SettingsEntity
    ) {

        viewModelScope.launch {

            repository.saveSettings(
                settings
            )
        }
    }
}


// ================================================================
// VIEWMODEL FACTORY
// ================================================================

class SmartSilenceViewModelFactory(
    private val repository: SmartSilenceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                SmartSilenceViewModel::class.java
            )
        ) {

            return SmartSilenceViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}