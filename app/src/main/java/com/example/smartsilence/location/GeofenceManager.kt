package com.example.smartsilence.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import androidx.annotation.RequiresPermission

import com.example.smartsilence.data.LocationEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(
    private val context: Context
) {

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {

        val intent = Intent(
            context,
            GeofenceBroadcastReceiver::class.java
        ).apply {
            action = "com.example.smartsilence.GEOFENCE_EVENT"
        }

        PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_MUTABLE
        )
    }


    // =========================================================
    // REGISTER ALL LOCATIONS
    // =========================================================

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun registerAllGeofences(
        locations: List<LocationEntity>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        if (locations.isEmpty()) {
            onSuccess()
            return
        }

        val geofences = locations.map { location ->

            Geofence.Builder()
                .setRequestId(location.id.toString())

                .setCircularRegion(
                    location.latitude,
                    location.longitude,
                    location.radius
                )

                .setExpirationDuration(
                    Geofence.NEVER_EXPIRE
                )

                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT
                )

                .build()
        }


        val geofencingRequest =
            GeofencingRequest.Builder()

                .setInitialTrigger(
                    GeofencingRequest.INITIAL_TRIGGER_ENTER
                )

                .addGeofences(geofences)

                .build()


        geofencingClient
            .addGeofences(
                geofencingRequest,
                geofencePendingIntent
            )

            .addOnSuccessListener {
                onSuccess()
            }

            .addOnFailureListener { exception ->
                onError(exception)
            }
    }


    // =========================================================
    // REMOVE ALL GEOFENCES
    // =========================================================

    fun removeAllGeofences(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        geofencingClient
            .removeGeofences(geofencePendingIntent)

            .addOnSuccessListener {
                onSuccess()
            }

            .addOnFailureListener { exception ->
                onError(exception)
            }
    }


    // =========================================================
    // REMOVE ONE GEOFENCE
    // =========================================================

    fun removeGeofence(
        location: LocationEntity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        geofencingClient
            .removeGeofences(
                listOf(location.id.toString())
            )

            .addOnSuccessListener {
                onSuccess()
            }

            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}