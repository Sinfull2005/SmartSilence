package com.example.smartsilence.security

/**
 * Represents the real-time context used by the
 * Context-Aware Trust system.
 *
 * This is intentionally separate from RiskContext.
 *
 * RiskContext is used for protecting sensitive
 * application actions.
 *
 * ContextTrustState is used for deciding whether
 * Smart Silence should automatically apply DND
 * based on the user's current context.
 */
data class ContextTrustState(

    /**
     * Whether the user is currently inside one of
     * the saved Smart Silence locations.
     */
    val insideSavedLocation: Boolean,

    /**
     * Risk associated with the current location.
     *
     * 0   = trusted
     * 100 = highly uncertain/untrusted
     */
    val locationRisk: Int,

    /**
     * Current detected activity.
     *
     * Examples from the existing activity-recognition
     * system can be represented as:
     *
     * STILL
     * WALKING
     * RUNNING
     * CYCLING
     * DRIVING
     * UNKNOWN
     */
    val activity: String,

    /**
     * Current hour of the day.
     *
     * Range:
     * 0..23
     */
    val hourOfDay: Int,

    /**
     * Whether the Android device is currently unlocked.
     */
    val deviceUnlocked: Boolean,

    /**
     * Whether the current movement/action pattern
     * differs from the user's recent behaviour.
     *
     * 0.0 = normal
     * 1.0 = highly unusual
     */
    val behaviourDeviation: Float,

    /**
     * Whether the same contextual action is occurring
     * repeatedly within a short period.
     */
    val rapidRepeatedAction: Boolean,

    /**
     * Number of recent failed authentication attempts.
     */
    val failedAttempts: Int
) {

    /**
     * Ensures all externally supplied values remain
     * inside their expected ranges.
     */
    fun normalized(): ContextTrustState {

        return copy(

            locationRisk =
                locationRisk.coerceIn(
                    0,
                    100
                ),

            activity =
                activity
                    .trim()
                    .ifEmpty {
                        "UNKNOWN"
                    },

            hourOfDay =
                hourOfDay.coerceIn(
                    0,
                    23
                ),

            behaviourDeviation =
                behaviourDeviation.coerceIn(
                    0f,
                    1f
                ),

            failedAttempts =
                failedAttempts.coerceAtLeast(
                    0
                )
        )
    }
}