package com.example.smartsilence.security

/**
 * Represents the interpreted output of the TensorFlow Lite model.
 *
 * The TFLite model produces a normalized risk value:
 *
 * 0.0 -> very low risk
 * 1.0 -> very high risk
 *
 * This class converts that value into two easy-to-use
 * 0-100 scores:
 *
 * mlRiskScore
 *     0   -> lowest risk
 *     100 -> highest risk
 *
 * behaviourScore
 *     100 -> most trustworthy behaviour
 *     0   -> least trustworthy behaviour
 */
data class MlBehaviourScore(
    val mlRiskScore: Int,
    val behaviourScore: Int
) {

    companion object {

        private const val MIN_SCORE = 0
        private const val MAX_SCORE = 100

        /**
         * Converts the raw TFLite output into a
         * normalized ML behaviour score.
         */
        fun fromModelOutput(
            modelOutput: Float
        ): MlBehaviourScore {

            val normalizedRisk =
                modelOutput.coerceIn(
                    0f,
                    1f
                )

            val riskScore =
                (normalizedRisk * MAX_SCORE)
                    .toInt()
                    .coerceIn(
                        MIN_SCORE,
                        MAX_SCORE
                    )

            val behaviourScore =
                (MAX_SCORE - riskScore)
                    .coerceIn(
                        MIN_SCORE,
                        MAX_SCORE
                    )

            return MlBehaviourScore(
                mlRiskScore = riskScore,
                behaviourScore = behaviourScore
            )
        }
    }
}