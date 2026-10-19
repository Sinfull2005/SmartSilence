package com.example.smartsilence.security

/**
 * Final result of an authentication attempt.
 */
sealed class AuthenticationResult {

    /**
     * Authentication succeeded.
     */
    data class Success(
        val strength: AuthenticationStrength
    ) : AuthenticationResult()

    /**
     * Authentication failed.
     */
    data class Failure(
        val reason: String
    ) : AuthenticationResult()

    /**
     * Authentication was cancelled by the user.
     */
    data object Cancelled : AuthenticationResult()

    /**
     * Authentication could not be started because
     * the required authentication method is unavailable.
     */
    data class Unavailable(
        val reason: String
    ) : AuthenticationResult()
}