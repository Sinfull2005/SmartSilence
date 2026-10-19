package com.example.smartsilence.security

import kotlin.math.exp

/**
 * Smart Silence ML risk model.
 *
 * This is a small logistic-regression model used as the
 * initial on-device risk model.
 *
 * The output is a value between 0.0 and 1.0:
 *
 * 0.0  -> very low risk
 * 1.0  -> very high risk
 *
 * The model is intentionally isolated from RiskEngine so
 * that it can later be replaced by a TensorFlow Lite model
 * without changing the rest of the security architecture.
 */
class RiskMlModel {

    /**
     * Calculates the probability of elevated risk.
     */
    fun predict(context: RiskContext): Float {

        val locationRisk =
            if (
                context.insideSavedLocation &&
                context.trustedLocation
            ) {
                0.0f
            } else {
                0.35f
            }

        val activityRisk =
            when (context.activity.uppercase()) {

                "STILL" -> 0.0f

                "WALKING" -> 0.05f

                "RUNNING" -> 0.10f

                "CYCLING" -> 0.20f

                "DRIVING" -> 0.30f

                else -> 0.15f
            }

        val timeRisk =
            when (context.hourOfDay) {

                in 0..5 -> 0.25f

                in 6..22 -> 0.0f

                else -> 0.10f
            }

        val failedAttemptRisk =
            when {

                context.failedAttempts <= 0 ->
                    0.0f

                context.failedAttempts == 1 ->
                    0.20f

                context.failedAttempts == 2 ->
                    0.40f

                context.failedAttempts >= 3 ->
                    0.65f

                else ->
                    0.0f
            }

        val deviceRisk =
            if (context.deviceUnlocked) {
                0.0f
            } else {
                0.25f
            }

        val repeatedActionRisk =
            if (context.rapidRepeatedAction) {
                0.20f
            } else {
                0.0f
            }

        val behaviourRisk =
            context.behaviourDeviation
                .coerceIn(0f, 1f)

        /*
         * Feature weights.
         *
         * These weights form the initial model and are kept
         * in one place so the model can later be replaced
         * by trained TensorFlow Lite weights.
         */
        val weightedScore =
            (-1.60f) +

                    (1.80f * locationRisk) +

                    (1.20f * activityRisk) +

                    (0.90f * timeRisk) +

                    (2.20f * failedAttemptRisk) +

                    (0.80f * deviceRisk) +

                    (1.10f * repeatedActionRisk) +

                    (2.00f * behaviourRisk)

        return sigmoid(
            weightedScore
        )
    }

    /**
     * Logistic sigmoid function.
     */
    private fun sigmoid(
        value: Float
    ): Float {

        val result =
            1.0 /
                    (
                            1.0 +
                                    exp(
                                        -value.toDouble()
                                    )
                            )

        return result
            .toFloat()
            .coerceIn(0f, 1f)
    }
}