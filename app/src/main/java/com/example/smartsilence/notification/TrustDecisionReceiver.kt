package com.example.smartsilence.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.example.smartsilence.MainActivity
import com.example.smartsilence.security.ContextTrustLevel
import com.example.smartsilence.security.DndBehaviorHistory

class TrustDecisionReceiver : BroadcastReceiver() {

    companion object {

        private const val TAG =
            "SmartSilenceTrust"

        const val ACTION_ENABLE_DND =
            "com.example.smartsilence.ENABLE_DND_FROM_TRUST_PROMPT"

        const val ACTION_KEEP_NORMAL =
            "com.example.smartsilence.KEEP_NORMAL_FROM_TRUST_PROMPT"

        const val EXTRA_TRUST_SCORE =
            "trust_score"

        const val EXTRA_REQUEST_AUTHENTICATED_DND =
            "request_authenticated_dnd"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val applicationContext =
            context.applicationContext

        val trustScore =
            intent.getIntExtra(
                EXTRA_TRUST_SCORE,
                50
            ).coerceIn(
                50,
                79
            )

        val history =
            DndBehaviorHistory(
                applicationContext
            )

        when (intent.action) {

            // =================================================
            // USER CHOSE ENABLE DND
            // =================================================

            ACTION_ENABLE_DND -> {

                Log.d(
                    TAG,
                    "User requested DND from medium-confidence prompt."
                )

                /*
                 * IMPORTANT:
                 *
                 * Do NOT enable DND directly here.
                 *
                 * MainActivity will run the existing adaptive
                 * authentication flow using:
                 *
                 * SensitiveAction.CHANGE_DND_RULES
                 *
                 * DND will only be enabled after authentication
                 * succeeds.
                 */

                val activityIntent =
                    Intent(
                        applicationContext,
                        MainActivity::class.java
                    ).apply {

                        putExtra(
                            EXTRA_REQUEST_AUTHENTICATED_DND,
                            true
                        )

                        putExtra(
                            EXTRA_TRUST_SCORE,
                            trustScore
                        )

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                    }

                applicationContext.startActivity(
                    activityIntent
                )

                Log.d(
                    TAG,
                    "MainActivity launched for authenticated DND request."
                )
            }


            // =================================================
            // USER CHOSE KEEP NORMAL
            // =================================================

            ACTION_KEEP_NORMAL -> {

                history.record(

                    trustScore =
                        trustScore,

                    trustLevel =
                        ContextTrustLevel.MEDIUM,

                    decision =
                        DndBehaviorHistory
                            .DECISION_USER_KEPT_NORMAL
                )

                Log.d(
                    TAG,
                    "User kept normal mode from medium-confidence prompt. " +
                            "Trust score: $trustScore"
                )
            }


            // =================================================
            // UNKNOWN ACTION
            // =================================================

            else -> {

                Log.w(
                    TAG,
                    "Unknown trust decision action: " +
                            intent.action
                )
            }
        }
    }
}