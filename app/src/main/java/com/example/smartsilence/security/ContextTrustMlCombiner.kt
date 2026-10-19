package com.example.smartsilence.security

/**
 * Safely combines the existing deterministic trust score
 * with the optional TensorFlow Lite behaviour score.
 *
 * Security principle:
 *
 * The ML model may reduce trust, but it must never
 * increase trust beyond the deterministic rule score.
 *
 * If ML is unavailable, the original rule score is
 * returned unchanged.
 */
class ContextTrustMlCombiner {

    companion object {

        /**
         * Deterministic rule engine remains the primary signal.
         */
        private const val RULE_WEIGHT = 0.70f

        /**
         * ML behaviour is an additional signal.
         */
        private const val ML_WEIGHT = 0.30f

        private const val MIN_SCORE = 0
        private const val MAX_SCORE = 100
    }

    /**
     * Combines the existing rule-based trust score
     * with the optional ML behaviour score.
     *
     * @param ruleTrustScore Existing deterministic trust score.
     * @param mlBehaviourScore Optional ML behaviour score.
     *
     * @return Final trust score from 0 to 100.
     */
    fun combine(
        ruleTrustScore: Int,
        mlBehaviourScore: MlBehaviourScore?
    ): Int {

        val normalizedRuleScore =
            ruleTrustScore.coerceIn(
                MIN_SCORE,
                MAX_SCORE
            )

        /**
         * No ML result means that the existing
         * rule-based system remains fully responsible.
         */
        if (mlBehaviourScore == null) {
            return normalizedRuleScore
        }

        val normalizedMlScore =
            mlBehaviourScore.behaviourScore.coerceIn(
                MIN_SCORE,
                MAX_SCORE
            )

        val weightedScore =
            (
                    normalizedRuleScore * RULE_WEIGHT +
                            normalizedMlScore * ML_WEIGHT
                    )
                .toInt()
                .coerceIn(
                    MIN_SCORE,
                    MAX_SCORE
                )

        /**
         * Security cap:
         *
         * ML can reduce trust, but can never raise
         * trust above the deterministic rule score.
         */
        return minOf(
            normalizedRuleScore,
            weightedScore
        )
    }
}