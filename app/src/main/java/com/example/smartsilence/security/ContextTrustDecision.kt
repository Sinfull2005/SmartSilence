package com.example.smartsilence.security

data class ContextTrustDecision(
    val trustScore: Int,
    val trustLevel: ContextTrustLevel,
    val shouldAutoApplyDnd: Boolean,
    val shouldAskUser: Boolean,
    val shouldBlockDnd: Boolean
)

class ContextTrustDecisionPolicy {

    fun evaluate(trustScore: Int): ContextTrustDecision {
        val normalizedScore = trustScore.coerceIn(0, 100)

        return when {
            normalizedScore >= 80 -> {
                ContextTrustDecision(
                    trustScore = normalizedScore,
                    trustLevel = ContextTrustLevel.HIGH,
                    shouldAutoApplyDnd = true,
                    shouldAskUser = false,
                    shouldBlockDnd = false
                )
            }

            normalizedScore >= 50 -> {
                ContextTrustDecision(
                    trustScore = normalizedScore,
                    trustLevel = ContextTrustLevel.MEDIUM,
                    shouldAutoApplyDnd = false,
                    shouldAskUser = true,
                    shouldBlockDnd = false
                )
            }

            else -> {
                ContextTrustDecision(
                    trustScore = normalizedScore,
                    trustLevel = ContextTrustLevel.LOW,
                    shouldAutoApplyDnd = false,
                    shouldAskUser = false,
                    shouldBlockDnd = true
                )
            }
        }
    }

    fun evaluate(context: ContextTrustState): ContextTrustDecision {
        val calculator = ContextTrustCalculator()
        val trustScore = calculator.calculate(context)
        return evaluate(trustScore)
    }
}