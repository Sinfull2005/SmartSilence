
package com.example.smartsilence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.example.smartsilence.data.LocationEntity
import com.example.smartsilence.location.GeofenceManager

private const val GEOFENCE_TAG = "SmartSilenceGeofence"

@Composable
fun RegisterSavedGeofences(
    context: Context,
    locations: List<LocationEntity>,
    locationsLoaded: Boolean,
    geofenceManager: GeofenceManager
) {
    LaunchedEffect(locations, locationsLoaded, context, geofenceManager) {
        // Do not unregister valid geofences while Room is still loading.
        if (!locationsLoaded) {
            Log.d(GEOFENCE_TAG, "Waiting for saved locations to load")
            return@LaunchedEffect
        }

        val finePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!finePermission) {
            Log.w(
                GEOFENCE_TAG,
                "Precise location permission is required to register geofences"
            )
            return@LaunchedEffect
        }

        val backgroundPermission =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

        if (!backgroundPermission) {
            Log.w(
                GEOFENCE_TAG,
                "Background location permission not granted"
            )
            return@LaunchedEffect
        }

        try {
            geofenceManager.removeAllGeofences(
                onSuccess = {
                    Log.d(
                        GEOFENCE_TAG,
                        "Old geofences removed; registering ${locations.size} saved locations"
                    )

                    geofenceManager.registerAllGeofences(
                        locations = locations,
                        onSuccess = {
                            Log.d(
                                GEOFENCE_TAG,
                                "Successfully registered ${locations.size} geofences"
                            )
                        },
                        onError = { exception ->
                            Log.e(
                                GEOFENCE_TAG,
                                "Failed to register geofences",
                                exception
                            )
                        }
                    )
                },
                onError = { exception ->
                    Log.e(
                        GEOFENCE_TAG,
                        "Failed to remove old geofences",
                        exception
                    )
                }
            )
        } catch (exception: SecurityException) {
            Log.e(
                GEOFENCE_TAG,
                "Location permission missing during geofence registration",
                exception
            )
        } catch (exception: Exception) {
            Log.e(
                GEOFENCE_TAG,
                "Unexpected geofence registration failure",
                exception
            )
        }
    }
}
