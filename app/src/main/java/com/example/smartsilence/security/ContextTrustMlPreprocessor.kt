package com.example.smartsilence.security

/**
 * Prepares ContextTrustState for TensorFlow Lite inference.
 *
 * This class performs deterministic preprocessing only.
 * It does not perform ML inference and does not make DND decisions.
 */
class ContextTrustMlPreprocessor {

    fun preprocess(
        context: ContextTrustState
    ): FloatArray {

        val features =
            ContextTrustMlFeatures.fromContext(context)

        val values =
            features.toFloatArray()

        return values.map { value ->
            value.coerceIn(0f, 1f)
        }.toFloatArray()
    }

    fun featureCount(): Int {
        return ContextTrustMlFeatures.FEATURE_NAMES.size
    }

    fun featureNames(): List<String> {
        return ContextTrustMlFeatures.FEATURE_NAMES
    }
}