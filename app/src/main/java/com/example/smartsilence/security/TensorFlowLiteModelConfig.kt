package com.example.smartsilence.security

object TensorFlowLiteModelConfig {

    /**
     * Name of the future trained model inside:
     *
     * app/src/main/assets/
     */
    const val MODEL_ASSET_NAME =
        "smart_silence_trust_model.tflite"

    /**
     * Number of input features expected by the model.
     *
     * This must remain synchronized with
     * ContextTrustMlFeatures.toFloatArray().
     */
    const val INPUT_FEATURE_COUNT = 12

    /**
     * Expected number of output values.
     *
     * The first version of the model will produce
     * one normalized score.
     */
    const val OUTPUT_VALUE_COUNT = 1
}