package com.example.smartsilence.security

import android.content.Context

/**
 * Stores and manages recent failed authentication attempts
 * for the Smart Silence adaptive security system.
 *
 * The count is persisted using SharedPreferences so it
 * survives app restarts.
 */
class FailedAuthenticationTracker(
    context: Context
) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    /**
     * Returns the current number of failed authentication
     * attempts.
     */
    fun getFailedAttempts(): Int {
        return preferences.getInt(
            KEY_FAILED_ATTEMPTS,
            0
        )
    }

    /**
     * Records one final authentication failure.
     *
     * After the configured number of consecutive failures,
     * a temporary security lockout is started.
     */
    fun recordFailure() {

        val currentAttempts =
            getFailedAttempts()

        val newAttempts =
            currentAttempts + 1

        val editor =
            preferences.edit()
                .putInt(
                    KEY_FAILED_ATTEMPTS,
                    newAttempts
                )

        if (newAttempts >= LOCKOUT_AFTER_FAILURES) {

            editor.putLong(
                KEY_LOCKOUT_UNTIL,
                System.currentTimeMillis() +
                        LOCKOUT_DURATION_MILLIS
            )
        }

        editor.apply()
    }

    /**
     * Returns true while the temporary authentication
     * lockout is active.
     */
    fun isLockedOut(): Boolean {

        val lockoutUntil =
            preferences.getLong(
                KEY_LOCKOUT_UNTIL,
                0L
            )

        if (lockoutUntil <= 0L) {
            return false
        }

        if (System.currentTimeMillis() >= lockoutUntil) {

            preferences.edit()
                .remove(KEY_LOCKOUT_UNTIL)
                .apply()

            return false
        }

        return true
    }

    /**
     * Returns the remaining lockout duration
     * in milliseconds.
     */
    fun getRemainingLockoutMillis(): Long {

        if (!isLockedOut()) {
            return 0L
        }

        val lockoutUntil =
            preferences.getLong(
                KEY_LOCKOUT_UNTIL,
                0L
            )

        return (
                lockoutUntil -
                        System.currentTimeMillis()
                ).coerceAtLeast(0L)
    }

    /**
     * Resets the failed-attempt count and any active
     * lockout after successful authentication.
     */
    fun resetAfterSuccess() {

        preferences.edit()
            .putInt(
                KEY_FAILED_ATTEMPTS,
                0
            )
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    /**
     * Updates the failed-attempt count according
     * to the final authentication result.
     *
     * Success:
     *     reset the count and lockout.
     *
     * Failure:
     *     increase the count and start a temporary
     *     lockout after repeated failures.
     *
     * Cancelled:
     *     do not change the count.
     *
     * Unavailable:
     *     do not change the count because authentication
     *     was not actually attempted.
     */
    fun recordResult(
        result: AuthenticationResult
    ) {

        when (result) {

            is AuthenticationResult.Success -> {

                resetAfterSuccess()
            }

            is AuthenticationResult.Failure -> {

                recordFailure()
            }

            AuthenticationResult.Cancelled -> {

                // Cancellation is not treated as a failed attempt.
            }

            is AuthenticationResult.Unavailable -> {

                // Authentication was unavailable,
                // so this is not counted as a failed attempt.
            }
        }
    }

    companion object {

        private const val PREFERENCES_NAME =
            "smart_silence_security"

        private const val KEY_FAILED_ATTEMPTS =
            "failed_authentication_attempts"

        private const val KEY_LOCKOUT_UNTIL =
            "authentication_lockout_until"

        private const val LOCKOUT_AFTER_FAILURES =
            5

        private const val LOCKOUT_DURATION_MILLIS =
            30_000L
    }
}