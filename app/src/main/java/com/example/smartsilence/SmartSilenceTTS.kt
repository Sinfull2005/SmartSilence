package com.example.smartsilence.location

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class SmartSilenceTTS(
    context: Context
) {

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private var textToSpeech: TextToSpeech? = null

    private var isReady = false

    private var pendingMessage: String? = null

    private var pendingOnDone: (() -> Unit)? = null


    // =========================================================
    // INITIALIZE
    // =========================================================

    init {

        textToSpeech =
            TextToSpeech(
                context.applicationContext
            ) { status ->

                if (
                    status ==
                    TextToSpeech.SUCCESS
                ) {

                    val result =
                        textToSpeech?.setLanguage(
                            Locale.getDefault()
                        )

                    isReady =
                        result != TextToSpeech.LANG_MISSING_DATA &&
                                result != TextToSpeech.LANG_NOT_SUPPORTED

                    textToSpeech?.setSpeechRate(
                        1.0f
                    )

                    textToSpeech?.setOnUtteranceProgressListener(

                        object :
                            UtteranceProgressListener() {

                            override fun onStart(
                                utteranceId: String?
                            ) {
                            }


                            override fun onDone(
                                utteranceId: String?
                            ) {

                                if (
                                    utteranceId ==
                                    "SmartSilenceSpeech"
                                ) {

                                    val action =
                                        pendingOnDone

                                    pendingOnDone = null

                                    mainHandler.post {

                                        action?.invoke()
                                    }
                                }
                            }


                            override fun onError(
                                utteranceId: String?
                            ) {

                                if (
                                    utteranceId ==
                                    "SmartSilenceSpeech"
                                ) {

                                    val action =
                                        pendingOnDone

                                    pendingOnDone = null

                                    mainHandler.post {

                                        action?.invoke()
                                    }
                                }
                            }
                        }
                    )


                    /*
                     * If speak() was requested before
                     * initialization finished, speak now.
                     */

                    val message =
                        pendingMessage

                    val action =
                        pendingOnDone

                    pendingMessage = null
                    pendingOnDone = null

                    if (
                        message != null
                    ) {

                        speakInternal(
                            message,
                            action
                        )
                    }
                }
            }
    }


    // =========================================================
    // SPEAK
    // =========================================================

    fun speak(
        message: String,
        onDone: (() -> Unit)? = null
    ) {

        if (!isReady) {

            pendingMessage =
                message

            pendingOnDone =
                onDone

            return
        }

        speakInternal(
            message,
            onDone
        )
    }


    // =========================================================
    // INTERNAL SPEECH
    // =========================================================

    private fun speakInternal(
        message: String,
        onDone: (() -> Unit)?
    ) {

        pendingOnDone =
            onDone

        textToSpeech?.speak(

            message,

            TextToSpeech.QUEUE_FLUSH,

            null,

            "SmartSilenceSpeech"
        )
    }


    // =========================================================
    // SHUTDOWN
    // =========================================================

    fun shutdown() {

        pendingMessage = null
        pendingOnDone = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()

        textToSpeech = null

        isReady = false
    }
}
