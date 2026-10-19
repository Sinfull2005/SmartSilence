package com.example.smartsilence.security

/**
 * Calculates the Context-Aware Trust Score.
 *
 * Final score:
 *
 * 80 - 100  -> HIGH TRUST
 * 50 - 79   -> MEDIUM TRUST
 * 0  - 49   -> LOW TRUST
 *
 * The score is always bounded between 0 and 100.
 */
class ContextTrustCalculator {

    companion object {

        private const val LOCATION_WEIGHT = 35
        private const val ACTIVITY_WEIGHT = 25
        private const val TIME_WEIGHT = 10
        private const val DEVICE_WEIGHT = 10
        private const val BEHAVIOUR_WEIGHT = 10
        private const val AUTHENTICATION_WEIGHT = 10

        private const val MAX_SCORE = 100

        private const val HIGH_TRUST_THRESHOLD = 80
        private const val MEDIUM_TRUST_THRESHOLD = 50
    }

    /**
     * Calculates the final trust score.
     */
    fun calculate(
        context: ContextTrustState
    ): Int {

        val normalizedContext =
            context.normalized()

        val locationTrust =
            calculateLocationTrust(
                normalizedContext
            )

        val activityTrust =
            calculateActivityTrust(
                normalizedContext
            )

        val timeTrust =
            calculateTimeTrust(
                normalizedContext
            )

        val deviceTrust =
            calculateDeviceTrust(
                normalizedContext
            )

        val behaviourTrust =
            calculateBehaviourTrust(
                normalizedContext
            )

        val authenticationTrust =
            calculateAuthenticationTrust(
                normalizedContext
            )

        val weightedScore =
            locationTrust * LOCATION_WEIGHT / 100f +
                    activityTrust * ACTIVITY_WEIGHT / 100f +
                    timeTrust * TIME_WEIGHT / 100f +
                    deviceTrust * DEVICE_WEIGHT / 100f +
                    behaviourTrust * BEHAVIOUR_WEIGHT / 100f +
                    authenticationTrust * AUTHENTICATION_WEIGHT / 100f

        return weightedScore
            .toInt()
            .coerceIn(
                0,
                MAX_SCORE
            )
    }

    /**
     * Converts location risk into location trust.
     *
     * 0 risk   -> 100 trust
     * 100 risk -> 0 trust
     */
    private fun calculateLocationTrust(
        context: ContextTrustState
    ): Int {

        if (!context.insideSavedLocation) {
            return 0
        }

        return (
                100 - context.locationRisk
                ).coerceIn(
                0,
                100
            )
    }

    /**
     * Calculates trust from the detected activity.
     *
     * Known activities are treated as stronger context
     * than UNKNOWN because Smart Silence has more
     * information about the user's current situation.
     */
    private fun calculateActivityTrust(
        context: ContextTrustState
    ): Int {

        return when (
            context.activity
                .trim()
                .uppercase()
        ) {

            "STILL" ->
                100

            "WALKING" ->
                90

            "RUNNING" ->
                85

            "CYCLING" ->
                85

            "DRIVING" ->
                90

            "IN_VEHICLE" ->
                90

            "ON_FOOT" ->
                90

            "UNKNOWN" ->
                40

            else ->
                50
        }
    }

    /**
     * Calculates trust based on time of day.
     *
     * Normal daytime/evening hours receive higher trust.
     * Very late-night/early-morning activity receives
     * reduced trust because it is a less predictable
     * context.
     */
    private fun calculateTimeTrust(
        context: ContextTrustState
    ): Int {

        return when (context.hourOfDay) {

            in 0..5 ->
                40

            in 6..8 ->
                75

            in 9..18 ->
                100

            in 19..22 ->
                85

            else ->
                60
        }
    }

    /**
     * The device being unlocked provides stronger
     * evidence that the current context is active.
     */
    private fun calculateDeviceTrust(
        context: ContextTrustState
    ): Int {

        return if (context.deviceUnlocked) {
            100
        } else {
            30
        }
    }

    /**
     * Converts behaviour deviation into behaviour trust.
     *
     * 0.0 deviation -> 100 trust
     * 1.0 deviation -> 0 trust
     */
    private fun calculateBehaviourTrust(
        context: ContextTrustState
    ): Int {

        val deviation =
            context.behaviourDeviation
                .coerceIn(
                    0f,
                    1f
                )

        return (
                100f -
                        (deviation * 100f)
                )
            .toInt()
            .coerceIn(
                0,
                100
            )
    }

    /**
     * Authentication history contributes to trust.
     *
     * Zero failed attempts gives maximum trust.
     * Repeated failures progressively reduce trust.
     */
    private fun calculateAuthenticationTrust(
        context: ContextTrustState
    ): Int {

        val failedAttempts =
            context.failedAttempts
                .coerceAtLeast(0)

        if (failedAttempts == 0) {
            return 100
        }

        val reduction =
            failedAttempts * 20

        return (
                100 - reduction
                ).coerceIn(
                0,
                100
            )
    }

    /**
     * Returns the trust category for a score.
     */
    fun getTrustLevel(
        trustScore: Int
    ): ContextTrustLevel {

        val normalizedScore =
            trustScore.coerceIn(
                0,
                MAX_SCORE
            )

        return when {

            normalizedScore >=
                    HIGH_TRUST_THRESHOLD ->
                ContextTrustLevel.HIGH

            normalizedScore >=
                    MEDIUM_TRUST_THRESHOLD ->
                ContextTrustLevel.MEDIUM

            else ->
                ContextTrustLevel.LOW
        }
    }
}

/**
 * Trust categories used by the Context-Aware
 * DND decision system.
 */
enum class ContextTrustLevel {

    HIGH,

    MEDIUM,

    LOW
}