package com.example.smartsilence

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationManager(
    private val context: Context
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onLocationReceived: (Location) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )
            .addOnSuccessListener { location ->

                if (location != null) {
                    onLocationReceived(location)
                } else {
                    onError(
                        Exception("Unable to get current location")
                    )
                }
            }
            .addOnFailureListener { exception ->

                onError(exception)
            }
    }
}