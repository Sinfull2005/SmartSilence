package com.example.smartsilence.security

import com.example.smartsilence.data.LocationEntity
import kotlin.math.*

/**
 * Calculates security risk based on the user's current
 * location and the saved Smart Silence locations.
 *
 * This class does not request location permission and does
 * not start location tracking. It only compares coordinates
 * that are already available.
 */
class LocationRiskCalculator {

    /**
     * Calculates the location risk for the current position.
     *
     * Risk levels:
     *
     * 0   = inside a saved location
     * 25  = close to a saved location
     * 50  = outside all saved locations
     *
     * If there are no saved locations, the location is treated
     * as untrusted and receives a risk of 50.
     */
    fun calculateRisk(
        currentLatitude: Double,
        currentLongitude: Double,
        savedLocations: List<LocationEntity>
    ): Int {

        if (savedLocations.isEmpty()) {
            return 50
        }

        val nearestLocation = savedLocations.minByOrNull { location ->
            distanceInMeters(
                currentLatitude = currentLatitude,
                currentLongitude = currentLongitude,
                savedLatitude = location.latitude,
                savedLongitude = location.longitude
            )
        } ?: return 50

        val distance = distanceInMeters(
            currentLatitude = currentLatitude,
            currentLongitude = currentLongitude,
            savedLatitude = nearestLocation.latitude,
            savedLongitude = nearestLocation.longitude
        )

        val radius = nearestLocation.radius.toDouble()

        return when {
            distance <= radius -> 0
            distance <= radius + NEARBY_DISTANCE_METERS -> 25
            else -> 50
        }
    }

    /**
     * Returns true when the current position is inside
     * at least one saved Smart Silence location.
     */
    fun isInsideSavedLocation(
        currentLatitude: Double,
        currentLongitude: Double,
        savedLocations: List<LocationEntity>
    ): Boolean {

        return savedLocations.any { location ->

            val distance = distanceInMeters(
                currentLatitude = currentLatitude,
                currentLongitude = currentLongitude,
                savedLatitude = location.latitude,
                savedLongitude = location.longitude
            )

            distance <= location.radius.toDouble()
        }
    }

    /**
     * Returns the saved location containing the current
     * position, or null when the user is outside all
     * saved locations.
     */
    fun findContainingLocation(
        currentLatitude: Double,
        currentLongitude: Double,
        savedLocations: List<LocationEntity>
    ): LocationEntity? {

        return savedLocations.firstOrNull { location ->

            val distance = distanceInMeters(
                currentLatitude = currentLatitude,
                currentLongitude = currentLongitude,
                savedLatitude = location.latitude,
                savedLongitude = location.longitude
            )

            distance <= location.radius.toDouble()
        }
    }

    /**
     * Calculates the distance between two geographic
     * coordinates using the Haversine formula.
     *
     * Result is returned in meters.
     */
    private fun distanceInMeters(
        currentLatitude: Double,
        currentLongitude: Double,
        savedLatitude: Double,
        savedLongitude: Double
    ): Double {

        val earthRadiusMeters = 6_371_000.0

        val latitudeDifference =
            Math.toRadians(
                savedLatitude - currentLatitude
            )

        val longitudeDifference =
            Math.toRadians(
                savedLongitude - currentLongitude
            )

        val currentLatitudeRadians =
            Math.toRadians(currentLatitude)

        val savedLatitudeRadians =
            Math.toRadians(savedLatitude)

        val a =
            sin(latitudeDifference / 2).pow(2) +
                    cos(currentLatitudeRadians) *
                    cos(savedLatitudeRadians) *
                    sin(longitudeDifference / 2).pow(2)

        val c =
            2 * atan2(
                sqrt(a),
                sqrt(1 - a)
            )

        return earthRadiusMeters * c
    }

    companion object {

        /**
         * Distance beyond the saved radius that is still
         * considered relatively close to a trusted location.
         */
        private const val NEARBY_DISTANCE_METERS = 500.0
    }
}
