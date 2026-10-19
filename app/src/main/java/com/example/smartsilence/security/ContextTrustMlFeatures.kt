package com.example.smartsilence.security

/**
 * Converts the existing ContextTrustState into a deterministic
 * numerical feature vector suitable for a future TensorFlow Lite model.
 *
 * This class does not perform ML inference.
 * It only prepares the existing context data.
 */
data class ContextTrustMlFeatures(
    val insideSavedLocation: Float,
    val locationTrust: Float,
    val activityStill: Float,
    val activityWalking: Float,
    val activityRunning: Float,
    val activityCycling: Float,
    val activityDriving: Float,
    val timeOfDay: Float,
    val deviceUnlocked: Float,
    val behaviourTrust: Float,
    val rapidRepeatedAction: Float,
    val failedAuthenticationLevel: Float
) {

    /**
     * Returns the features in the exact order that should be
     * supplied to a future ML model.
     */
    fun toFloatArray(): FloatArray {
        return floatArrayOf(
            insideSavedLocation,
            locationTrust,
            activityStill,
            activityWalking,
            activityRunning,
            activityCycling,
            activityDriving,
            timeOfDay,
            deviceUnlocked,
            behaviourTrust,
            rapidRepeatedAction,
            failedAuthenticationLevel
        )
    }

    companion object {

        /**
         * The feature order is kept explicit so that the same
         * ordering can later be used when preparing TFLite input.
         */
        val FEATURE_NAMES = listOf(
            "inside_saved_location",
            "location_trust",
            "activity_still",
            "activity_walking",
            "activity_running",
            "activity_cycling",
            "activity_driving",
            "time_of_day",
            "device_unlocked",
            "behaviour_trust",
            "rapid_repeated_action",
            "failed_authentication_level"
        )

        /**
         * Creates ML features from the existing context model.
         */
        fun fromContext(
            context: ContextTrustState
        ): ContextTrustMlFeatures {

            val normalizedContext =
                context.normalized()

            val activity =
                normalizedContext.activity
                    .trim()
                    .uppercase()

            val locationTrust =
                (
                        100 -
                                normalizedContext.locationRisk
                        )
                    .coerceIn(0, 100)
                    .toFloat() / 100f

            val timeOfDay =
                normalizedContext.hourOfDay
                    .coerceIn(0, 23)
                    .toFloat() / 23f

            val behaviourTrust =
                (
                        1f -
                                normalizedContext.behaviourDeviation
                        )
                    .coerceIn(0f, 1f)

            val failedAuthenticationLevel =
                normalizedContext.failedAttempts
                    .coerceIn(0, 5)
                    .toFloat() / 5f

            return ContextTrustMlFeatures(
                insideSavedLocation =
                    if (
                        normalizedContext.insideSavedLocation
                    ) {
                        1f
                    } else {
                        0f
                    },

                locationTrust =
                    locationTrust,

                activityStill =
                    if (activity == "STILL") 1f else 0f,

                activityWalking =
                    if (
                        activity == "WALKING" ||
                        activity == "ON_FOOT"
                    ) {
                        1f
                    } else {
                        0f
                    },

                activityRunning =
                    if (activity == "RUNNING") 1f else 0f,

                activityCycling =
                    if (activity == "CYCLING") 1f else 0f,

                activityDriving =
                    if (
                        activity == "DRIVING" ||
                        activity == "IN_VEHICLE"
                    ) {
                        1f
                    } else {
                        0f
                    },

                timeOfDay =
                    timeOfDay,

                deviceUnlocked =
                    if (
                        normalizedContext.deviceUnlocked
                    ) {
                        1f
                    } else {
                        0f
                    },

                behaviourTrust =
                    behaviourTrust,

                rapidRepeatedAction =
                    if (
                        normalizedContext.rapidRepeatedAction
                    ) {
                        1f
                    } else {
                        0f
                    },

                failedAuthenticationLevel =
                    failedAuthenticationLevel
            )
        }
    }
}