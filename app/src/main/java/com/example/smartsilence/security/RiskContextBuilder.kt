package com.example.smartsilence.security

import android.content.Context

import com.example.smartsilence.data.LocationEntity

import java.util.Calendar

/**
 * Builds the RiskContext used by the adaptive
 * authentication system.
 *
 * Signals currently collected:
 *
 * - action sensitivity
 * - current location
 * - saved Smart Silence locations
 * - failed authentication attempts
 * - current time
 * - rapid repeated sensitive actions
 * - device lock state
 * - behavioural deviation
 */
class RiskContextBuilder(
    context: Context,
    private val locationRiskCalculator:
    LocationRiskCalculator =
        LocationRiskCalculator()
) {

    private val deviceSecuritySignalProvider =
        DeviceSecuritySignalProvider(
            context.applicationContext
        )

    private val securityBehaviourTracker =
        SecurityBehaviourTracker(
            context.applicationContext
        )

    private var lastAction:
            SensitiveAction? = null

    private var lastActionTimeMillis =
        0L

    private companion object {

        const val RAPID_ACTION_WINDOW_MILLIS =
            10_000L

        const val LOCATION_UNAVAILABLE_RISK =
            50
    }

    /**
     * Creates a RiskContext when current location
     * is available.
     */
    fun build(
        action: SensitiveAction,
        currentLatitude: Double,
        currentLongitude: Double,
        savedLocations: List<LocationEntity>,
        failedAttempts: Int
    ): RiskContext {

        val locationRisk =
            locationRiskCalculator.calculateRisk(
                currentLatitude =
                    currentLatitude,

                currentLongitude =
                    currentLongitude,

                savedLocations =
                    savedLocations
            )

        val insideSavedLocation =
            locationRiskCalculator.isInsideSavedLocation(
                currentLatitude =
                    currentLatitude,

                currentLongitude =
                    currentLongitude,

                savedLocations =
                    savedLocations
            )

        val hourOfDay =
            Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY)

        val currentTimeMillis =
            System.currentTimeMillis()

        val rapidRepeatedAction =
            lastAction == action &&
                    currentTimeMillis -
                    lastActionTimeMillis <=
                    RAPID_ACTION_WINDOW_MILLIS

        val deviceUnlocked =
            deviceSecuritySignalProvider
                .isDeviceUnlocked()

        val behaviourDeviation =
            securityBehaviourTracker
                .recordAction(
                    action
                )

        lastAction =
            action

        lastActionTimeMillis =
            currentTimeMillis

        return RiskContext(

            actionSensitivity =
                action.sensitivity,

            locationRisk =
                locationRisk,

            failedAttempts =
                failedAttempts,

            insideSavedLocation =
                insideSavedLocation,

            trustedLocation =
                insideSavedLocation,

            hourOfDay =
                hourOfDay,

            deviceUnlocked =
                deviceUnlocked,

            rapidRepeatedAction =
                rapidRepeatedAction,

            behaviourDeviation =
                behaviourDeviation
        )
    }

    /**
     * Creates a RiskContext when current location
     * cannot be obtained.
     */
    fun buildWithoutLocation(
        action: SensitiveAction,
        failedAttempts: Int
    ): RiskContext {

        val hourOfDay =
            Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY)

        val currentTimeMillis =
            System.currentTimeMillis()

        val rapidRepeatedAction =
            lastAction == action &&
                    currentTimeMillis -
                    lastActionTimeMillis <=
                    RAPID_ACTION_WINDOW_MILLIS

        val deviceUnlocked =
            deviceSecuritySignalProvider
                .isDeviceUnlocked()

        val behaviourDeviation =
            securityBehaviourTracker
                .recordAction(
                    action
                )

        lastAction =
            action

        lastActionTimeMillis =
            currentTimeMillis

        return RiskContext(

            actionSensitivity =
                action.sensitivity,

            locationRisk =
                LOCATION_UNAVAILABLE_RISK,

            insideSavedLocation =
                false,

            trustedLocation =
                false,

            failedAttempts =
                failedAttempts,

            hourOfDay =
                hourOfDay,

            deviceUnlocked =
                deviceUnlocked,

            rapidRepeatedAction =
                rapidRepeatedAction,

            behaviourDeviation =
                behaviourDeviation
        )
    }
}