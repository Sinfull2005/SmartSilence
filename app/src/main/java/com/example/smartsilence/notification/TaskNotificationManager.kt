package com.example.smartsilence.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.example.smartsilence.MainActivity
import com.example.smartsilence.R
import com.example.smartsilence.data.TaskEntity

class TaskNotificationManager(
    private val context: Context
) {

    companion object {

        private const val CHANNEL_ID =
            "smart_silence_location_tasks"

        private const val CHANNEL_NAME =
            "Location Task Reminders"

        private const val CHANNEL_DESCRIPTION =
            "Task reminders triggered when entering saved locations"

        private const val CHANNEL_IMPORTANCE =
            NotificationManager.IMPORTANCE_HIGH
    }


    // ============================================================
    // CREATE NOTIFICATION CHANNEL
    // ============================================================

    fun createNotificationChannel() {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            CHANNEL_IMPORTANCE
        ).apply {

            description = CHANNEL_DESCRIPTION

            enableVibration(true)
        }

        notificationManager.createNotificationChannel(
            channel
        )
    }


    // ============================================================
    // SHOW TASK NOTIFICATION
    // ============================================================

    fun showTaskNotification(
        task: TaskEntity
    ) {

        /*
         * Android 13+ requires the POST_NOTIFICATIONS
         * runtime permission.
         *
         * If permission is not granted, simply return.
         * This prevents the notification system from
         * interfering with the existing DND/geofence flow.
         */

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }


        // --------------------------------------------------------
        // Open MainActivity when notification is tapped
        // --------------------------------------------------------

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        }


        val pendingIntent =
            PendingIntent.getActivity(
                context,
                task.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )


        // --------------------------------------------------------
        // Build notification
        // --------------------------------------------------------

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    task.title
                )
                .setContentText(
                    if (task.description.isBlank()) {
                        "You have a task reminder for this location."
                    } else {
                        task.description
                    }
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            if (task.description.isBlank()) {
                                "You have a task reminder for this location."
                            } else {
                                task.description
                            }
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()


        // --------------------------------------------------------
        // Display notification
        // --------------------------------------------------------

        NotificationManagerCompat
            .from(context)
            .notify(
                1000 + task.id,
                notification
            )
    }
}