package com.example.smartsilence.security

import android.content.Context
import java.util.Calendar

/**
 * Collects the contextual signals required by the
 * Context-Aware Trust system.
 *
 * This class does NOT change DND behaviour.
 *
 * It only collects and combines the signals that will
 * later be evaluated by the trust algorithm.
 */
class ContextTrustCollector(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val deviceSecuritySignalProvider =
        DeviceSecuritySignalProvider(
            applicationContext
        )

    private val securityBehaviourTracker =
        SecurityBehaviourTracker(
            applicationContext
        )

    private val failedAuthenticationTracker =
        FailedAuthenticationTracker(
            applicationContext
        )

    /**
     * Builds the current context state.
     *
     * The caller supplies the location and activity
     * information already detected by Smart Silence.
     *
     * No DND action is performed here.
     */
    fun collect(
        insideSavedLocation: Boolean,
        locationRisk: Int,
        activity: String,
        rapidRepeatedAction: Boolean,
        behaviourAction: SensitiveAction
    ): ContextTrustState {

        val calendar =
            Calendar.getInstance()

        val hourOfDay =
            calendar.get(Calendar.HOUR_OF_DAY)

        val deviceUnlocked =
            deviceSecuritySignalProvider
                .isDeviceUnlocked()

        val behaviourDeviation =
            securityBehaviourTracker
                .recordAction(
                    behaviourAction
                )

        val failedAttempts =
            failedAuthenticationTracker
                .getFailedAttempts()

        return ContextTrustState(

            insideSavedLocation =
                insideSavedLocation,

            locationRisk =
                locationRisk,

            activity =
                activity,

            hourOfDay =
                hourOfDay,

            deviceUnlocked =
                deviceUnlocked,

            behaviourDeviation =
                behaviourDeviation,

            rapidRepeatedAction =
                rapidRepeatedAction,

            failedAttempts =
                failedAttempts
        ).normalized()
    }

    /**
     * Creates a safe default context when an activity
     * signal is temporarily unavailable.
     *
     * UNKNOWN is deliberately used instead of guessing
     * the user's activity.
     */
    fun collectWithoutActivity(
        insideSavedLocation: Boolean,
        locationRisk: Int,
        rapidRepeatedAction: Boolean,
        behaviourAction: SensitiveAction
    ): ContextTrustState {

        return collect(

            insideSavedLocation =
                insideSavedLocation,

            locationRisk =
                locationRisk,

            activity =
                "UNKNOWN",

            rapidRepeatedAction =
                rapidRepeatedAction,

            behaviourAction =
                behaviourAction
        )
    }
}