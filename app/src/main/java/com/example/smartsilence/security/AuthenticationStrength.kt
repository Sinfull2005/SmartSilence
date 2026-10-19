package com.example.smartsilence.security

/**
 * Authentication strength selected by the adaptive
 * security system according to the calculated risk.
 */
enum class AuthenticationStrength {

    /**
     * Used for low-risk sensitive actions.
     *
     * The user can use a normal supported biometric
     * authentication method.
     */
    NORMAL,

    /**
     * Used when the risk is elevated.
     *
     * The authentication layer will require a stronger
     * authentication method.
     */
    STRONG
}