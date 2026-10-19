package com.example.smartsilence.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import java.util.Locale

class RemoteLocationManager(
    private val context: Context
) {

    fun searchLocation(
        query: String,
        onSuccess: (List<Address>) -> Unit,
        onError: (String) -> Unit
    ) {

        if (query.isBlank()) {
            onError("Please enter a location.")
            return
        }

        if (!Geocoder.isPresent()) {
            onError("Location search is not available on this device.")
            return
        }

        val geocoder =
            Geocoder(
                context,
                Locale.getDefault()
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            geocoder.getFromLocationName(
                query,
                5,
                object : Geocoder.GeocodeListener {

                    override fun onGeocode(
                        addresses: MutableList<Address>
                    ) {

                        if (addresses.isEmpty()) {

                            onError(
                                "No matching locations found."
                            )

                        } else {

                            onSuccess(addresses)
                        }
                    }

                    override fun onError(
                        errorMessage: String?
                    ) {

                        onError(
                            errorMessage
                                ?: "Unable to search for location."
                        )
                    }
                }
            )

        } else {

            Thread {

                try {

                    @Suppress("DEPRECATION")

                    val addresses =
                        geocoder.getFromLocationName(
                            query,
                            5
                        )

                    if (
                        addresses.isNullOrEmpty()
                    ) {

                        onError(
                            "No matching locations found."
                        )

                    } else {

                        onSuccess(addresses)
                    }

                } catch (exception: Exception) {

                    onError(
                        exception.message
                            ?: "Unable to search for location."
                    )
                }
            }.start()
        }
    }
}