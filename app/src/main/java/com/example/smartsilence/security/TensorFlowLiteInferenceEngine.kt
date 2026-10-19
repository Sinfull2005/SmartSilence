package com.example.smartsilence.security

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Isolated TensorFlow Lite inference layer.
 *
 * This class is responsible only for:
 * 1. Loading a TFLite model from app/src/main/assets.
 * 2. Running inference.
 * 3. Returning the model output.
 *
 * It does not make security or DND decisions.
 *
 * If the model is missing or inference fails, null is returned.
 * The caller can then safely use the existing rule-based fallback.
 */
class TensorFlowLiteInferenceEngine(
    context: Context,
    private val modelAssetName: String
) {

    companion object {
        private const val TAG = "SmartSilenceTFLite"
    }

    private val applicationContext =
        context.applicationContext

    private var interpreter: Interpreter? = null

    /**
     * Attempts to load the TFLite model.
     *
     * Returns true when the model is successfully loaded.
     */
    fun isModelAvailable(): Boolean {
        if (interpreter != null) {
            return true
        }

        return try {
            interpreter =
                Interpreter(
                    loadModelFile(modelAssetName)
                )

            Log.d(
                TAG,
                "TFLite model loaded successfully: $modelAssetName"
            )

            true
        } catch (exception: Exception) {
            interpreter = null

            Log.w(
                TAG,
                "TFLite model is unavailable: $modelAssetName",
                exception
            )

            false
        }
    }

    /**
     * Runs inference using the supplied feature vector.
     *
     * Expected input:
     * FloatArray containing the ML features.
     *
     * Expected output:
     * One floating-point model score.
     *
     * Returns null when the model is unavailable or inference fails.
     */
    fun runInference(
        input: FloatArray
    ): FloatArray? {

        if (!isModelAvailable()) {
            return null
        }

        val activeInterpreter =
            interpreter ?: return null

        return try {
            val output =
                Array(1) {
                    FloatArray(1)
                }

            activeInterpreter.run(
                arrayOf(input),
                output
            )

            output[0]
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "TFLite inference failed.",
                exception
            )

            null
        }
    }

    /**
     * Releases the TFLite interpreter.
     */
    fun close() {
        try {
            interpreter?.close()
        } catch (exception: Exception) {
            Log.w(
                TAG,
                "Failed to close TFLite interpreter.",
                exception
            )
        } finally {
            interpreter = null
        }
    }

    /**
     * Maps the model file stored in app/src/main/assets
     * into memory.
     */
    private fun loadModelFile(
        assetName: String
    ): MappedByteBuffer {

        val assetFileDescriptor =
            applicationContext.assets.openFd(
                assetName
            )

        FileInputStream(
            assetFileDescriptor.fileDescriptor
        ).use { inputStream ->

            val fileChannel =
                inputStream.channel

            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
        }
    }
}