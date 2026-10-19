
package com.example.smartsilence.location

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.smartsilence.data.SmartSilenceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Attempts to restore saved geofences after Android finishes booting.
 */
class GeofenceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val fineLocationGranted =
                    ContextCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                val backgroundLocationGranted =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                            ContextCompat.checkSelfPermission(
                                appContext,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                if (!fineLocationGranted || !backgroundLocationGranted) {
                    Log.w(
                        TAG,
                        "Boot recovery skipped: required location permission is missing"
                    )
                    return@launch
                }

                val locations = SmartSilenceDatabase
                    .getDatabase(appContext)
                    .smartSilenceDao()
                    .getAllLocations()
                    .first()

                val manager = GeofenceManager(appContext)

                manager.removeAllGeofences(
                    onSuccess = {
                        if (locations.isEmpty()) {
                            Log.i(TAG, "No saved locations to restore")
                            pendingResult.finish()
                            scope.cancel()
                        } else {
                            manager.registerAllGeofences(
                                locations = locations,
                                onSuccess = {
                                    Log.i(
                                        TAG,
                                        "Restored ${locations.size} saved geofences after boot"
                                    )
                                    pendingResult.finish()
                                    scope.cancel()
                                },
                                onError = { error ->
                                    Log.e(TAG, "Geofence restoration failed", error)
                                    pendingResult.finish()
                                    scope.cancel()
                                }
                            )
                        }
                    },
                    onError = { error ->
                        Log.e(TAG, "Could not remove old geofences during boot", error)
                        pendingResult.finish()
                        scope.cancel()
                    }
                )
            } catch (error: Exception) {
                Log.e(TAG, "Unexpected error during boot geofence recovery", error)
                pendingResult.finish()
                scope.cancel()
            }
        }
    }

    private companion object {
        const val TAG = "SmartSilenceGeofence"
    }
}
