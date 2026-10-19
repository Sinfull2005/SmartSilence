package com.example.smartsilence

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

class DndManager(
    private val context: Context
) {

    private val notificationManager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager


    // =========================================================
    // CHECK DND ACCESS
    // =========================================================

    fun hasDndAccess(): Boolean {

        return notificationManager
            .isNotificationPolicyAccessGranted
    }


    // =========================================================
    // OPEN DND SETTINGS
    // =========================================================

    fun openDndSettings() {

        val intent =
            Intent(
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        context.startActivity(intent)
    }


    // =========================================================
    // CHECK SMART SILENCE STATE
    // =========================================================

    fun isDndEnabled(): Boolean {

        return notificationManager.currentInterruptionFilter ==
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
    }

    // =========================================================
    // ENABLE SMART SILENCE
    // =========================================================

    fun enableDnd(): Boolean {

        if (!hasDndAccess()) {
            return false
        }

        return try {

            /*
             * Smart Silence needs the phone-call category
             * available to the call-screening service.
             *
             * Non-priority calls will then be silenced by
             * SmartSilenceCallScreeningService.
             *
             * Priority contacts will be allowed to ring.
             */

            val currentPolicy =
                notificationManager.notificationPolicy

            val updatedCategories =
                currentPolicy.priorityCategories or
                        NotificationManager.Policy.PRIORITY_CATEGORY_CALLS

            val updatedPolicy =
                NotificationManager.Policy(
                    updatedCategories,
                    NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                    currentPolicy.priorityMessageSenders,
                    currentPolicy.suppressedVisualEffects
                )

            notificationManager.setNotificationPolicy(
                updatedPolicy
            )

            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            )

            /*
             * Confirm that Android actually accepted
             * the requested DND state.
             */

            isDndEnabled()

        } catch (exception: SecurityException) {

            exception.printStackTrace()

            false
        }
    }


    // =========================================================
    // DISABLE SMART SILENCE
    // =========================================================

    fun disableDnd(): Boolean {

        if (!hasDndAccess()) {
            return false
        }

        return try {

            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_ALL
            )

            !isDndEnabled()

        } catch (exception: SecurityException) {

            exception.printStackTrace()

            false
        }
    }
}
