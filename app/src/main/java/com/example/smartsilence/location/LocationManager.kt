package com.example.smartsilence.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationManager(
    private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {

        // Check location permission
        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {

            onError(
                "Location permission is not granted."
            )

            return
        }

        // Request a fresh location
        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
            )
                .setWaitForAccurateLocation(true)
                .setMaxUpdates(1)
                .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {

                override fun onLocationResult(
                    locationResult:
                    com.google.android.gms.location.LocationResult
                ) {

                    val location =
                        locationResult.lastLocation

                    if (location != null) {

                        onLocationReceived(
                            location.latitude,
                            location.longitude
                        )

                    } else {

                        onError(
                            "Unable to obtain current location."
                        )
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }
            },
            android.os.Looper.getMainLooper()
        )
    }
}