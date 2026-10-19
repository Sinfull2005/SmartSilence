
package com.example.smartsilence.security

import android.content.Context

/**
 * Stores only the local authentication session's expiry timestamp.
 * Never stores the user's PIN, password, or pattern.
 */
class LocalSessionManager(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun startSession(
        nowMillis: Long = System.currentTimeMillis()
    ) {
        preferences.edit()
            .putLong(
                KEY_EXPIRES_AT,
                nowMillis + SESSION_DURATION_MILLIS
            )
            .commit()
    }

    @Synchronized
    fun isSessionValid(
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val expiresAt = preferences.getLong(
            KEY_EXPIRES_AT,
            0L
        )

        if (expiresAt <= nowMillis) {
            clearSession()
            return false
        }

        return true
    }

    fun getRemainingMillis(
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        return (
                preferences.getLong(KEY_EXPIRES_AT, 0L) -
                        nowMillis
                ).coerceAtLeast(0L)
    }

    @Synchronized
    fun clearSession() {
        preferences.edit()
            .remove(KEY_EXPIRES_AT)
            .commit()
    }

    companion object {
        private const val PREFERENCES_NAME =
            "smart_silence_local_session"

        private const val KEY_EXPIRES_AT =
            "session_expires_at"

        const val SESSION_DURATION_MILLIS =
            5 * 60 * 1000L
    }
}
