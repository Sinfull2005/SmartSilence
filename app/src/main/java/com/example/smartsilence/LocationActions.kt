package com.example.smartsilence

import android.util.Log

import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.location.GeofenceManager
import com.example.smartsilence.viewmodel.SmartSilenceViewModel

fun editLocation(
    oldLocation: LocationEntity,
    newName: String,
    newLatitude: Double,
    newLongitude: Double,
    newRadius: Float,
    geofenceManager: GeofenceManager,
    viewModel: SmartSilenceViewModel
) {
    geofenceManager.removeGeofence(
        location = oldLocation,
        onSuccess = {
            Log.d(
                "SmartSilenceGeofence",
                "Old geofence removed: ${oldLocation.name}"
            )
        },
        onError = { exception ->
            Log.e(
                "SmartSilenceGeofence",
                "Could not remove old geofence",
                exception
            )
        }
    )

    viewModel.deleteLocation(oldLocation)

    viewModel.addLocation(
        name = newName,
        latitude = newLatitude,
        longitude = newLongitude,
        radius = newRadius
    )
}

fun deleteLocation(
    location: LocationEntity,
    geofenceManager: GeofenceManager,
    viewModel: SmartSilenceViewModel
) {
    geofenceManager.removeGeofence(
        location = location,
        onSuccess = {
            Log.d(
                "SmartSilenceGeofence",
                "Geofence removed: ${location.name}"
            )
        },
        onError = { exception ->
            Log.e(
                "SmartSilenceGeofence",
                "Failed to remove geofence",
                exception
            )
        }
    )

    viewModel.deleteLocation(location)
}