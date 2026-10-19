package com.example.smartsilence.security

import android.content.Context
import android.util.Log

class ContextTrustMlFallback(
    context: Context
) {

    companion object {
        private const val TAG = "SmartSilenceML"
    }

    private val mlInference =
        ContextTrustMlInference(
            context.applicationContext
        )

    private val ruleCalculator =
        ContextTrustCalculator()

    private val combiner =
        ContextTrustMlCombiner()

    /**
     * Calculates the final trust score.
     *
     * The existing rule-based trust score is always calculated first.
     *
     * If the ML model is unavailable or inference fails,
     * the rule-based score is returned unchanged.
     *
     * If ML is available, its behavioural score is combined
     * with the rule-based score using the existing safety combiner.
     */
    fun calculateFinalTrustScore(
        context: ContextTrustState
    ): Int {

        // -----------------------------------------------------
        // 1. Existing deterministic rule-based score
        // -----------------------------------------------------

        val ruleTrustScore =
            ruleCalculator.calculate(context)

        Log.d(
            TAG,
            "Rule-based trust score: $ruleTrustScore"
        )


        // -----------------------------------------------------
        // 2. Try ML inference
        // -----------------------------------------------------

        val mlBehaviourScore =
            try {
                mlInference.predict(context)
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "ML inference threw an exception. " +
                            "Falling back to rule score.",
                    exception
                )

                null
            }


        // -----------------------------------------------------
        // 3. ML unavailable -> safe fallback
        // -----------------------------------------------------

        if (mlBehaviourScore == null) {

            Log.w(
                TAG,
                "ML score unavailable. " +
                        "Using rule-based trust score: $ruleTrustScore"
            )

            return ruleTrustScore
        }


        // -----------------------------------------------------
        // 4. ML available -> combine with rules
        // -----------------------------------------------------

        val finalTrustScore =
            combiner.combine(
                ruleTrustScore = ruleTrustScore,
                mlBehaviourScore = mlBehaviourScore
            )

        Log.d(
            TAG,
            "ML behaviour score: " +
                    "${mlBehaviourScore.behaviourScore}"
        )

        Log.d(
            TAG,
            "Final combined trust score: $finalTrustScore"
        )

        return finalTrustScore
    }


    /**
     * Returns true when the actual TFLite/LiteRT model
     * is currently available.
     */
    fun isMlModelAvailable(): Boolean {
        return try {
            mlInference.isModelAvailable()
        } catch (exception: Exception) {

            Log.w(
                TAG,
                "Unable to determine ML model availability.",
                exception
            )

            false
        }
    }


    /**
     * Releases the underlying ML interpreter.
     */
    fun close() {
        mlInference.close()
    }
}