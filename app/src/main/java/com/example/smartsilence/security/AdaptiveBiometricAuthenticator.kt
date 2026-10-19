package com.example.smartsilence.security

import android.os.Build
import android.util.Log

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Handles the actual Android authentication prompt.
 *
 * RiskEngine and AdaptiveAuthenticationPolicy decide
 * how strong authentication should be.
 *
 * This class only performs that authentication.
 */
class AdaptiveBiometricAuthenticator(
    private val activity: FragmentActivity,
    private val onAuthenticationResult: (AuthenticationResult) -> Unit,
    private val onBiometricFailure: () -> Unit = {}
) {

    private val executor =
        ContextCompat.getMainExecutor(activity)

    private val biometricPrompt =
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    Log.d(
                        "SmartSilenceAuth",
                        "AUTH_SUCCESS"
                    )

                    /*
                     * The actual strength was supplied when
                     * authenticate() was called.
                     */
                    val strength =
                        currentAuthenticationStrength
                            ?: AuthenticationStrength.NORMAL

                    onAuthenticationResult(
                        AuthenticationResult.Success(
                            strength = strength
                        )
                    )

                    currentAuthenticationStrength = null
                }

                override fun onAuthenticationFailed() {

                    Log.d(
                        "SmartSilenceAuth",
                        "AUTH_FAILED_BIOMETRIC"
                    )

                    /*
                     * This callback means that the biometric
                     * did not match.
                     *
                     * Android normally keeps the biometric
                     * prompt open and allows another attempt.
                     *
                     * Notify the failed-attempt protection
                     * separately without treating this as a
                     * final authentication result.
                     */
                    onBiometricFailure()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {

                    Log.d(
                        "SmartSilenceAuth",
                        "AUTH_ERROR code=$errorCode message=$errString"
                    )

                    onAuthenticationResult(
                        when (errorCode) {

                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_CANCELED -> {
                                AuthenticationResult.Cancelled
                            }

                            BiometricPrompt.ERROR_NO_BIOMETRICS,
                            BiometricPrompt.ERROR_HW_NOT_PRESENT,
                            BiometricPrompt.ERROR_HW_UNAVAILABLE,
                            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                                AuthenticationResult.Unavailable(
                                    reason = errString.toString()
                                )
                            }

                            else -> {
                                AuthenticationResult.Failure(
                                    reason = errString.toString()
                                )
                            }
                        }
                    )

                    currentAuthenticationStrength = null
                }
            }
        )

    private var currentAuthenticationStrength:
            AuthenticationStrength? = null

    /**
     * Starts authentication according to the
     * adaptive authentication policy.
     */
    fun authenticate(
        strength: AuthenticationStrength
    ) {

        val authenticators =
            getAllowedAuthenticators(strength)

        val availability =
            BiometricManager
                .from(activity)
                .canAuthenticate(authenticators)

        if (
            availability != BiometricManager.BIOMETRIC_SUCCESS
        ) {

            onAuthenticationResult(
                AuthenticationResult.Unavailable(
                    reason =
                        getAvailabilityMessage(availability)
                )
            )

            return
        }

        currentAuthenticationStrength = strength

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(
                    when (strength) {
                        AuthenticationStrength.NORMAL ->
                            "Smart Silence Authentication"

                        AuthenticationStrength.STRONG ->
                            "Strong Authentication Required"
                    }
                )
                .setSubtitle(
                    when (strength) {
                        AuthenticationStrength.NORMAL ->
                            "Authenticate to continue"

                        AuthenticationStrength.STRONG ->
                            "Additional verification is required"
                    }
                )
                .setAllowedAuthenticators(
                    authenticators
                )
                .setConfirmationRequired(true)
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }

    /**
     * Determines which authentication methods are allowed.
     *
     * NORMAL:
     * Weak/strong biometric + device credential.
     *
     * STRONG:
     * Strong biometric.
     *
     * On Android 11+, device credential can safely be
     * combined with Class 3 biometric.
     *
     * On Android 9/10, that combination is unsupported,
     * so strong authentication uses Class 3 biometric only.
     */
    private fun getAllowedAuthenticators(
        strength: AuthenticationStrength
    ): Int {

        return when (strength) {

            AuthenticationStrength.NORMAL -> {

                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            }

            AuthenticationStrength.STRONG -> {

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.R
                ) {

                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL

                } else {

                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                }
            }
        }
    }

    private fun getAvailabilityMessage(
        status: Int
    ): String {

        return when (status) {

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "This device does not have biometric hardware."

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Biometric hardware is currently unavailable."

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "No biometric or device credential is configured."

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                "This authentication method is not supported."

            else ->
                "Authentication is currently unavailable."
        }
    }
}