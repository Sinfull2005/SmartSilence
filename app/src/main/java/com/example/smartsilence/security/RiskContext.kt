package com.example.smartsilence.security

/**
 * Context used by the Smart Silence adaptive
 * security system.
 *
 * The primary risk factors are:
 *
 * 1. Action sensitivity
 * 2. Current location risk
 * 3. Recent failed authentication attempts
 *
 * Additional context fields are retained for future
 * behavioural/context-aware security enhancements.
 */
data class RiskContext(

    /**
     * Base sensitivity of the action being protected.
     *
     * Range:
     * 0..100
     */
    val actionSensitivity: Int = 0,

    /**
     * Risk contribution calculated from the
     * user's current location.
     *
     * Range:
     * 0..100
     */
    val locationRisk: Int = 0,

    /**
     * Number of recent failed authentication attempts.
     */
    val failedAttempts: Int = 0,

    /**
     * Whether the user is currently inside a
     * saved Smart Silence location.
     */
    val insideSavedLocation: Boolean = false,

    /**
     * Whether the current location is trusted
     * by Smart Silence.
     */
    val trustedLocation: Boolean = false,

    /**
     * Current detected activity.
     *
     * Examples:
     *
     * STILL
     * WALKING
     * RUNNING
     * CYCLING
     * DRIVING
     * UNKNOWN
     */
    val activity: String = "UNKNOWN",

    /**
     * Current hour in 24-hour format.
     */
    val hourOfDay: Int = 12,

    /**
     * Whether the device is currently unlocked.
     */
    val deviceUnlocked: Boolean = true,

    /**
     * Whether another sensitive action happened
     * shortly before this authentication request.
     */
    val rapidRepeatedAction: Boolean = false,

    /**
     * Future behavioural-deviation signal.
     *
     * This can later be supplied by the behavioural
     * or ML security component.
     */
    val behaviourDeviation: Float = 0f
) {

    companion object {

        /**
         * Creates a risk context directly from
         * a protected Smart Silence action.
         */
        fun forAction(
            action: SensitiveAction,
            locationRisk: Int = 0,
            failedAttempts: Int = 0
        ): RiskContext {

            return RiskContext(
                actionSensitivity =
                    action.sensitivity,

                locationRisk =
                    locationRisk,

                failedAttempts =
                    failedAttempts
            )
        }
    }
}