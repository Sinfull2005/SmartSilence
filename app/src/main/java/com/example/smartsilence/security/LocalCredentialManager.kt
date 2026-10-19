package com.example.smartsilence.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Manages app-owned local credentials.
 *
 * Only the credential type, random salt, and derived verifier
 * are persisted. The original credential is never saved.
 */
class LocalCredentialManager(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    enum class CredentialType {
        PIN,
        PASSWORD,
        PATTERN
    }

    sealed class RegistrationResult {
        data object Success : RegistrationResult()
        data object AlreadyRegistered : RegistrationResult()
        data object StorageFailure : RegistrationResult()

        data class InvalidInput(
            val message: String
        ) : RegistrationResult()
    }

    fun isRegistered(): Boolean {
        return getCredentialType() != null &&
                !preferences.getString(KEY_SALT, null).isNullOrBlank() &&
                !preferences.getString(KEY_VERIFIER, null).isNullOrBlank()
    }

    fun getCredentialType(): CredentialType? {
        val storedType =
            preferences.getString(KEY_TYPE, null)
                ?: return null

        return try {
            CredentialType.valueOf(storedType)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Registers the first local credential.
     * Existing credentials cannot be overwritten here.
     */
    @Synchronized
    fun registerCredential(
        type: CredentialType,
        credential: String
    ): RegistrationResult {

        if (isRegistered()) {
            return RegistrationResult.AlreadyRegistered
        }

        val validationError = validateInput(type, credential)
        if (validationError != null) {
            return RegistrationResult.InvalidInput(validationError)
        }

        return storeCredential(type, credential)
    }

    /**
     * Replaces the credential after the recovery flow has
     * successfully verified the configured recovery answers.
     *
     * Call this method only after successful recovery verification.
     */
    @Synchronized
    fun replaceCredentialAfterRecovery(
        type: CredentialType,
        credential: String
    ): RegistrationResult {

        val validationError = validateInput(type, credential)
        if (validationError != null) {
            return RegistrationResult.InvalidInput(validationError)
        }

        return storeCredential(type, credential)
    }

    private fun storeCredential(
        type: CredentialType,
        credential: String
    ): RegistrationResult {
        var salt: ByteArray? = null
        var verifier: ByteArray? = null

        return try {
            val normalized = normalizeCredential(type, credential)

            salt = ByteArray(SALT_LENGTH_BYTES).also {
                SecureRandom().nextBytes(it)
            }

            verifier = deriveVerifier(
                normalized.toCharArray(),
                salt
            )

            val saved = preferences.edit()
                .putString(KEY_TYPE, type.name)
                .putString(
                    KEY_SALT,
                    Base64.encodeToString(salt, Base64.NO_WRAP)
                )
                .putString(
                    KEY_VERIFIER,
                    Base64.encodeToString(verifier, Base64.NO_WRAP)
                )
                .putInt(KEY_VERSION, STORAGE_VERSION)
                .commit()

            if (saved) {
                RegistrationResult.Success
            } else {
                RegistrationResult.StorageFailure
            }
        } catch (_: Exception) {
            RegistrationResult.StorageFailure
        } finally {
            salt?.fill(0)
            verifier?.fill(0)
        }
    }

    @Synchronized
    fun verifyCredential(
        type: CredentialType,
        credential: String
    ): Boolean {

        if (!isRegistered()) return false
        if (getCredentialType() != type) return false
        if (validateInput(type, credential) != null) return false

        var salt: ByteArray? = null
        var expectedVerifier: ByteArray? = null
        var actualVerifier: ByteArray? = null

        return try {
            val storedSalt =
                preferences.getString(KEY_SALT, null)
                    ?: return false

            val storedVerifier =
                preferences.getString(KEY_VERIFIER, null)
                    ?: return false

            salt = Base64.decode(storedSalt, Base64.NO_WRAP)
            expectedVerifier =
                Base64.decode(storedVerifier, Base64.NO_WRAP)

            if (
                salt.size != SALT_LENGTH_BYTES ||
                expectedVerifier.size != VERIFIER_LENGTH_BYTES ||
                preferences.getInt(KEY_VERSION, -1) != STORAGE_VERSION
            ) {
                return false
            }

            actualVerifier = deriveVerifier(
                normalizeCredential(type, credential).toCharArray(),
                salt
            )

            MessageDigest.isEqual(
                expectedVerifier,
                actualVerifier
            )
        } catch (_: Exception) {
            false
        } finally {
            salt?.fill(0)
            expectedVerifier?.fill(0)
            actualVerifier?.fill(0)
        }
    }

    fun validateInput(
        type: CredentialType,
        credential: String
    ): String? {
        return when (type) {
            CredentialType.PIN -> {
                if (
                    credential.length !in 6..12 ||
                    credential.any { it !in '0'..'9' }
                ) {
                    "PIN must contain 6–12 digits."
                } else {
                    null
                }
            }

            CredentialType.PASSWORD -> {
                if (
                    credential.length !in 8..128 ||
                    credential.any { it.isISOControl() }
                ) {
                    "Password must contain 8–128 characters."
                } else {
                    null
                }
            }

            CredentialType.PATTERN -> {
                val pattern = normalizePattern(credential)

                if (
                    pattern == null ||
                    pattern.length !in 4..9 ||
                    pattern.toSet().size != pattern.length
                ) {
                    "Pattern must connect at least 4 distinct dots, numbered 1–9."
                } else {
                    null
                }
            }
        }
    }

    private fun normalizeCredential(
        type: CredentialType,
        credential: String
    ): String {
        return when (type) {
            CredentialType.PIN,
            CredentialType.PASSWORD -> credential

            CredentialType.PATTERN ->
                normalizePattern(credential)
                    ?: throw IllegalArgumentException("Invalid pattern")
        }
    }

    private fun normalizePattern(input: String): String? {
        val pattern = input.filterNot {
            it == '-' ||
                    it == ',' ||
                    it == ' ' ||
                    it == '\t'
        }

        if (pattern.any { it !in '1'..'9' }) return null
        return pattern
    }

    private fun deriveVerifier(
        credential: CharArray,
        salt: ByteArray
    ): ByteArray {
        val spec = PBEKeySpec(
            credential,
            salt,
            PBKDF2_ITERATIONS,
            VERIFIER_LENGTH_BYTES * 8
        )

        return try {
            SecretKeyFactory
                .getInstance(KDF_ALGORITHM)
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
            credential.fill('\u0000')
        }
    }

    companion object {
        private const val PREFERENCES_NAME =
            "smart_silence_local_credentials"

        private const val KEY_TYPE = "credential_type"
        private const val KEY_SALT = "credential_salt"
        private const val KEY_VERIFIER = "credential_verifier"
        private const val KEY_VERSION = "credential_version"

        private const val STORAGE_VERSION = 1
        private const val SALT_LENGTH_BYTES = 16
        private const val VERIFIER_LENGTH_BYTES = 32
        private const val PBKDF2_ITERATIONS = 210_000
        private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
    }
}