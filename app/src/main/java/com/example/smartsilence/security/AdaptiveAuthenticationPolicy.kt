package com.example.smartsilence.security

/**
 * Result produced by the adaptive authentication policy.
 */
data class AuthenticationPolicyResult(

    /**
     * Risk assessment that produced this decision.
     */
    val riskAssessment: RiskAssessmentResult,

    /**
     * Authentication strength required.
     */
    val authenticationStrength: AuthenticationStrength,

    /**
     * Whether additional authentication is required.
     */
    val authenticationRequired: Boolean
)

/**
 * Determines the authentication requirement
 * from the dynamically calculated risk level.
 *
 * LOW:
 * No additional authentication.
 *
 * MEDIUM:
 * Normal biometric/device authentication.
 *
 * HIGH:
 * Strong authentication.
 *
 * CRITICAL:
 * Strong authentication.
 */
class AdaptiveAuthenticationPolicy(
    private val riskEngine: RiskEngine =
        RiskEngine()
) {

    /**
     * Evaluates the current security context.
     */
    fun evaluate(
        context: RiskContext
    ): AuthenticationPolicyResult {

        val riskAssessment =
            riskEngine.assess(
                context
            )

        // -----------------------------------------------------
        // AUTHENTICATION REQUIREMENT
        // -----------------------------------------------------

        val authenticationRequired =
            riskAssessment.riskLevel !=
                    RiskLevel.LOW

        // -----------------------------------------------------
        // AUTHENTICATION STRENGTH
        // -----------------------------------------------------

        val authenticationStrength =
            when (
                riskAssessment.riskLevel
            ) {

                RiskLevel.LOW ->
                    AuthenticationStrength.NORMAL

                RiskLevel.MEDIUM ->
                    AuthenticationStrength.NORMAL

                RiskLevel.HIGH ->
                    AuthenticationStrength.STRONG

                RiskLevel.CRITICAL ->
                    AuthenticationStrength.STRONG
            }

        return AuthenticationPolicyResult(

            riskAssessment =
                riskAssessment,

            authenticationStrength =
                authenticationStrength,

            authenticationRequired =
                authenticationRequired
        )
    }
}