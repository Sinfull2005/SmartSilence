package com.example.smartsilence.security

import android.content.Context
import android.util.Log

/**
 * Connects ContextTrustState preprocessing to the
 * TensorFlow Lite inference engine.
 *
 * This class produces an optional ML behaviour score.
 *
 * It does not make DND decisions.
 *
 * If the TFLite model is unavailable or inference fails,
 * null is returned so the existing rule-based system
 * can continue safely.
 */
class ContextTrustMlInference(
    context: Context
) {

    companion object {
        private const val TAG = "SmartSilenceML"
    }

    private val preprocessor =
        ContextTrustMlPreprocessor()

    private val inferenceEngine =
        TensorFlowLiteInferenceEngine(
            context = context.applicationContext,
            modelAssetName =
                TensorFlowLiteModelConfig.MODEL_ASSET_NAME
        )

    /**
     * Runs ML inference for the supplied context.
     *
     * Returns an MlBehaviourScore when the model
     * is available and produces a valid output.
     *
     * Returns null when ML is unavailable.
     */
    fun predict(
        context: ContextTrustState
    ): MlBehaviourScore? {

        val features =
            preprocessor.preprocess(context)

        if (
            features.size !=
            TensorFlowLiteModelConfig.INPUT_FEATURE_COUNT
        ) {
            Log.e(
                TAG,
                "Unexpected feature count: ${features.size}. " +
                        "Expected: " +
                        TensorFlowLiteModelConfig.INPUT_FEATURE_COUNT
            )

            return null
        }

        val output =
            inferenceEngine.runInference(features)
                ?: return null

        if (
            output.size !=
            TensorFlowLiteModelConfig.OUTPUT_VALUE_COUNT
        ) {
            Log.e(
                TAG,
                "Unexpected ML output count: ${output.size}. " +
                        "Expected: " +
                        TensorFlowLiteModelConfig.OUTPUT_VALUE_COUNT
            )

            return null
        }

        val behaviourScore =
            MlBehaviourScore.fromModelOutput(
                modelOutput = output[0]
            )

        Log.d(
            TAG,
            "ML risk=${behaviourScore.mlRiskScore}, " +
                    "behaviour=${behaviourScore.behaviourScore}"
        )

        return behaviourScore
    }

    /**
     * Indicates whether the configured TFLite model
     * can currently be loaded.
     */
    fun isModelAvailable(): Boolean {
        return inferenceEngine.isModelAvailable()
    }

    /**
     * Releases the TFLite interpreter.
     */
    fun close() {
        inferenceEngine.close()
    }
}