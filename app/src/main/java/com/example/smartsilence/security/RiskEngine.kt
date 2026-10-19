package com.example.smartsilence.security

import android.util.Log

/**
 * Result returned by the Smart Silence
 * adaptive risk engine.
 */
data class RiskAssessmentResult(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val actionSensitivity: Int,
    val locationRisk: Int,
    val failedAttemptRisk: Int,
    val contextSignalRisk: Int
)

/**
 * Smart Silence adaptive security risk engine.
 *
 * Signals:
 * - Action sensitivity
 * - Location risk
 * - Failed authentication attempts
 * - Time of day
 * - Rapid repeated actions
 * - Device state
 * - Behavioural deviation
 */
class RiskEngine {

    companion object {
        private const val TAG = "SmartSilenceRisk"
    }

    fun assess(context: RiskContext): RiskAssessmentResult {

        val actionSensitivity =
            context.actionSensitivity.coerceIn(0, 100)

        val locationRisk =
            context.locationRisk.coerceIn(0, 100)

        val failedAttemptRisk =
            calculateFailedAttemptRisk(context.failedAttempts)

        val contextSignalRisk =
            calculateContextSignalRisk(context)

        val rawScore =
            actionSensitivity +
                    locationRisk +
                    failedAttemptRisk +
                    contextSignalRisk

        val finalScore = rawScore.coerceIn(0, 100)

        val riskLevel = when (finalScore) {
            in 0..29 -> RiskLevel.LOW
            in 30..59 -> RiskLevel.MEDIUM
            in 60..79 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }

        Log.d(
            TAG,
            """
            Risk breakdown:
            Action sensitivity = $actionSensitivity
            Location risk = $locationRisk
            Inside saved location = ${context.insideSavedLocation}
            Trusted location = ${context.trustedLocation}
            Failed attempts = ${context.failedAttempts}
            Failed-attempt risk = $failedAttemptRisk
            Hour = ${context.hourOfDay}
            Device unlocked = ${context.deviceUnlocked}
            Rapid repeated action = ${context.rapidRepeatedAction}
            Behaviour deviation = ${context.behaviourDeviation}
            Context signal risk = $contextSignalRisk
            Raw score = $rawScore
            Final score = $finalScore
            Risk level = $riskLevel
            """.trimIndent()
        )

        return RiskAssessmentResult(
            riskScore = finalScore,
            riskLevel = riskLevel,
            actionSensitivity = actionSensitivity,
            locationRisk = locationRisk,
            failedAttemptRisk = failedAttemptRisk,
            contextSignalRisk = contextSignalRisk
        )
    }

    private fun calculateFailedAttemptRisk(
        failedAttempts: Int
    ): Int {
        return when {
            failedAttempts <= 0 -> 0
            failedAttempts == 1 -> 10
            failedAttempts == 2 -> 20
            failedAttempts == 3 -> 35
            else -> 50
        }
    }

    /**
     * Context contribution is capped at 25 points.
     */
    private fun calculateContextSignalRisk(
        context: RiskContext
    ): Int {

        val timeRisk =
            if (context.hourOfDay in 0..5) 5 else 0

        val repeatedActionRisk =
            if (context.rapidRepeatedAction) 10 else 0

        val deviceStateRisk =
            if (context.deviceUnlocked) 0 else 10

        val behaviourRisk =
            (context.behaviourDeviation.coerceIn(0f, 1f) * 10f)
                .toInt()

        return (
                timeRisk +
                        repeatedActionRisk +
                        deviceStateRisk +
                        behaviourRisk
                ).coerceIn(0, 25)
    }
}
