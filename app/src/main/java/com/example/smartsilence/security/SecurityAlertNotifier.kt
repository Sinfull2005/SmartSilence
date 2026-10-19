package com.example.smartsilence.security

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class SecurityAlertNotifier(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    init {
        createNotificationChannel()
    }

    fun showAuthenticationLockoutAlert() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permissionGranted =
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                return
            }
        }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                SECURITY_CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_lock_lock
                )
                .setContentTitle(
                    "Smart Silence Security Alert"
                )
                .setContentText(
                    "Multiple failed authentication attempts were detected."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Multiple failed authentication attempts were detected. " +
                                    "Smart Silence has temporarily locked authentication " +
                                    "for your security."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                AUTHENTICATION_LOCKOUT_NOTIFICATION_ID,
                notification
            )
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager =
            applicationContext.getSystemService(
                NotificationManager::class.java
            )

        val channel =
            NotificationChannel(
                SECURITY_CHANNEL_ID,
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Security notifications from Smart Silence"
                enableVibration(true)
            }

        notificationManager.createNotificationChannel(channel)
    }

    companion object {

        private const val SECURITY_CHANNEL_ID =
            "smart_silence_security_alerts"

        private const val AUTHENTICATION_LOCKOUT_NOTIFICATION_ID =
            2001
    }
}