package com.example.smartsilence.security

import android.app.KeyguardManager
import android.content.Context

/**
 * Provides device-state information used by the
 * Smart Silence adaptive security system.
 *
 * Android remains responsible for the actual device
 * credential state. Smart Silence only reads whether
 * the device is currently unlocked.
 */
class DeviceSecuritySignalProvider(
    private val context: Context
) {

    /**
     * Returns true when the Android device is currently
     * considered unlocked.
     *
     * Smart Silence never stores or manages the
     * device credential itself.
     */
    fun isDeviceUnlocked(): Boolean {

        val keyguardManager =
            context.getSystemService(
                Context.KEYGUARD_SERVICE
            ) as? KeyguardManager
                ?: return true

        return !keyguardManager.isDeviceLocked
    }
}