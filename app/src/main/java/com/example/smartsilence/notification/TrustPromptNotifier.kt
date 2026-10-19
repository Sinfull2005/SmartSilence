package com.example.smartsilence.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class TrustPromptNotifier(
    private val context: Context
) {

    companion object {

        private const val CHANNEL_ID =
            "smart_silence_trust_prompt"

        private const val CHANNEL_NAME =
            "Smart Silence Trust Decisions"

        private const val NOTIFICATION_ID =
            3201
    }

    fun createNotificationChannel() {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    "Requests confirmation when Smart Silence has medium confidence in a DND decision."
            }

        notificationManager.createNotificationChannel(
            channel
        )
    }

    fun showTrustPrompt(
        trustScore: Int
    ) {

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
        }

        createNotificationChannel()

        val normalizedScore =
            trustScore.coerceIn(50, 79)

        val enableIntent =
            Intent(
                context,
                TrustDecisionReceiver::class.java
            ).apply {

                action =
                    TrustDecisionReceiver.ACTION_ENABLE_DND

                putExtra(
                    TrustDecisionReceiver.EXTRA_TRUST_SCORE,
                    normalizedScore
                )
            }

        val keepNormalIntent =
            Intent(
                context,
                TrustDecisionReceiver::class.java
            ).apply {

                action =
                    TrustDecisionReceiver.ACTION_KEEP_NORMAL

                putExtra(
                    TrustDecisionReceiver.EXTRA_TRUST_SCORE,
                    normalizedScore
                )
            }

        val enablePendingIntent =
            PendingIntent.getBroadcast(
                context,
                3202,
                enableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val keepNormalPendingIntent =
            PendingIntent.getBroadcast(
                context,
                3203,
                keepNormalIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_lock_silent_mode
                )
                .setContentTitle(
                    "Smart Silence needs confirmation"
                )
                .setContentText(
                    "Context confidence is $normalizedScore/100. Should DND be enabled?"
                )
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "Smart Silence detected a medium-confidence situation " +
                                "for DND. Trust score: $normalizedScore/100."
                    )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .addAction(
                    0,
                    "ENABLE DND",
                    enablePendingIntent
                )
                .addAction(
                    0,
                    "KEEP NORMAL",
                    keepNormalPendingIntent
                )
                .build()

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID,
            notification
        )
    }
}