
package com.example.smartsilence.security

import android.content.Context
import android.util.Log

/**
 * Coordinates rule-based risk assessment and optional
 * TensorFlow Lite inference for adaptive authentication.
 */
class SecurityAuthenticationManager(
    private val context: Context? = null,
    private val policy: AdaptiveAuthenticationPolicy =
        AdaptiveAuthenticationPolicy()
) {

    companion object {
        private const val TAG = "SmartSilenceML"
    }

    private val riskEventLogger: RiskEventLogger? by lazy {
        context?.applicationContext?.let { RiskEventLogger(it) }
    }

    private val mlInference: ContextTrustMlInference? by lazy {
        context?.applicationContext?.let { ContextTrustMlInference(it) }
    }

    fun createAuthenticationRequirement(
        context: RiskContext
    ): AuthenticationPolicyResult {

        val originalResult = policy.evaluate(context)
        val inference = mlInference

        if (inference == null) {
            recordRiskAssessment(
                action = "Sensitive action",
                ruleScore = originalResult.riskAssessment.riskScore,
                mlScore = null,
                combinedScore = originalResult.riskAssessment.riskScore,
                level = originalResult.riskAssessment.riskLevel.name,
                authenticationRequired = originalResult.authenticationRequired,
                context = context,
                note = "ML unavailable; rule-based assessment used"
            )
            return originalResult
        }

        val mlState = ContextTrustState(
            insideSavedLocation = context.insideSavedLocation,
            locationRisk = context.locationRisk,
            activity = context.activity,
            hourOfDay = context.hourOfDay,
            deviceUnlocked = context.deviceUnlocked,
            behaviourDeviation = context.behaviourDeviation,
            rapidRepeatedAction = context.rapidRepeatedAction,
            failedAttempts = context.failedAttempts
        )

        val mlScore = try {
            inference.predict(mlState)
        } catch (exception: Exception) {
            Log.e(TAG, "ML inference failed.", exception)
            null
        }

        if (mlScore == null) {
            recordRiskAssessment(
                action = "Sensitive action",
                ruleScore = originalResult.riskAssessment.riskScore,
                mlScore = null,
                combinedScore = originalResult.riskAssessment.riskScore,
                level = originalResult.riskAssessment.riskLevel.name,
                authenticationRequired = originalResult.authenticationRequired,
                context = context,
                note = "ML inference unavailable; rule-based assessment used"
            )
            return originalResult
        }

        val originalAssessment = originalResult.riskAssessment

        val combinedRiskScore = maxOf(
            originalAssessment.riskScore,
            mlScore.mlRiskScore.coerceIn(0, 100)
        )

        val combinedRiskLevel = when (combinedRiskScore) {
            in 0..29 -> RiskLevel.LOW
            in 30..59 -> RiskLevel.MEDIUM
            in 60..79 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }

        val combinedAssessment = originalAssessment.copy(
            riskScore = combinedRiskScore,
            riskLevel = combinedRiskLevel
        )

        val authenticationRequired =
            combinedRiskLevel != RiskLevel.LOW

        val authenticationStrength = when (combinedRiskLevel) {
            RiskLevel.LOW -> AuthenticationStrength.NORMAL
            RiskLevel.MEDIUM -> AuthenticationStrength.NORMAL
            RiskLevel.HIGH -> AuthenticationStrength.STRONG
            RiskLevel.CRITICAL -> AuthenticationStrength.STRONG
        }

        Log.d(
            TAG,
            "Rule risk=${originalAssessment.riskScore}, " +
                    "ML risk=${mlScore.mlRiskScore}, " +
                    "combined risk=$combinedRiskScore, " +
                    "level=$combinedRiskLevel"
        )

        recordRiskAssessment(
            action = "Sensitive action",
            ruleScore = originalAssessment.riskScore,
            mlScore = mlScore.mlRiskScore,
            combinedScore = combinedRiskScore,
            level = combinedRiskLevel.name,
            authenticationRequired = authenticationRequired,
            context = context,
            note = "Rule-based and ML assessment"
        )

        return AuthenticationPolicyResult(
            riskAssessment = combinedAssessment,
            authenticationStrength = authenticationStrength,
            authenticationRequired = authenticationRequired
        )
    }

    private fun recordRiskAssessment(
        action: String,
        ruleScore: Int,
        mlScore: Int?,
        combinedScore: Int,
        level: String,
        authenticationRequired: Boolean,
        context: RiskContext,
        note: String
    ) {
        try {
            riskEventLogger?.logEvent(
                eventType = "RISK_ASSESSMENT_$level",
                action = action,
                details = buildString {
                    append("Rule risk: $ruleScore/100; ")
                    append("ML risk: ${mlScore?.let { "$it/100" } ?: "unavailable"}; ")
                    append("Combined risk: $combinedScore/100; ")
                    append("Level: $level; ")
                    append("Authentication required: $authenticationRequired; ")
                    append("Action sensitivity: ${context.actionSensitivity}; ")
                    append("Location risk: ${context.locationRisk}; ")
                    append("Inside saved location: ${context.insideSavedLocation}; ")
                    append("Trusted location: ${context.trustedLocation}; ")
                    append("Failed attempts: ${context.failedAttempts}; ")
                    append("Activity: ${context.activity}; ")
                    append("Hour: ${context.hourOfDay}; ")
                    append("Device unlocked: ${context.deviceUnlocked}; ")
                    append("Rapid repeated action: ${context.rapidRepeatedAction}; ")
                    append(note)
                }
            )
        } catch (exception: Exception) {
            Log.w(TAG, "Unable to save risk assessment log.", exception)
        }
    }

    fun createAuthenticationRequirement(
        action: SensitiveAction,
        locationRisk: Int = 0,
        failedAttempts: Int = 0
    ): AuthenticationPolicyResult {
        val riskContext = RiskContext.forAction(
            action = action,
            locationRisk = locationRisk,
            failedAttempts = failedAttempts
        )

        return createAuthenticationRequirement(riskContext)
    }

    fun isAuthenticationSuccessful(
        result: AuthenticationResult
    ): Boolean = result is AuthenticationResult.Success

    fun close() {
        try {
            mlInference?.close()
        } catch (exception: Exception) {
            Log.w(TAG, "Unable to close ML inference engine.", exception)
        }
    }
}
