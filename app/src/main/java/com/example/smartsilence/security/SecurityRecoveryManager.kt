package com.example.smartsilence.security

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityRecoveryManager(context: Context) {

    private val applicationContext = context.applicationContext

    private val preferences =
        applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    /**
     * Stores the configured questions and encrypted answers.
     * Answers are not stored as plain text.
     */
    @Synchronized
    fun saveRecoveryQuestions(
        firstQuestion: String,
        firstAnswer: String,
        secondQuestion: String,
        secondAnswer: String
    ): Boolean {
        val question1 = firstQuestion.trim()
        val answer1 = normalizeAnswer(firstAnswer)
        val question2 = secondQuestion.trim()
        val answer2 = normalizeAnswer(secondAnswer)

        if (
            question1.isEmpty() ||
            answer1.isEmpty() ||
            question2.isEmpty() ||
            answer2.isEmpty()
        ) {
            return false
        }

        return try {
            val encryptedAnswer1 = encrypt(answer1)
            val encryptedAnswer2 = encrypt(answer2)

            preferences.edit()
                .putString(KEY_FIRST_QUESTION, question1)
                .putString(KEY_FIRST_ANSWER, encryptedAnswer1)
                .putString(KEY_SECOND_QUESTION, question2)
                .putString(KEY_SECOND_ANSWER, encryptedAnswer2)
                .putBoolean(KEY_RECOVERY_CONFIGURED, true)
                .commit()
        } catch (_: Exception) {
            false
        }
    }

    fun isRecoveryConfigured(): Boolean {
        return preferences.getBoolean(KEY_RECOVERY_CONFIGURED, false) &&
                !preferences.getString(KEY_FIRST_QUESTION, null).isNullOrBlank() &&
                !preferences.getString(KEY_SECOND_QUESTION, null).isNullOrBlank() &&
                !preferences.getString(KEY_FIRST_ANSWER, null).isNullOrBlank() &&
                !preferences.getString(KEY_SECOND_ANSWER, null).isNullOrBlank()
    }

    /**
     * Returns the questions only. Encrypted answers are never exposed.
     */
    fun getRecoveryQuestions(): List<String> {
        val firstQuestion =
            preferences.getString(KEY_FIRST_QUESTION, null)
        val secondQuestion =
            preferences.getString(KEY_SECOND_QUESTION, null)

        return listOfNotNull(firstQuestion, secondQuestion)
    }

    fun isRecoveryLockedOut(): Boolean {
        val until = preferences.getLong(KEY_LOCKOUT_UNTIL, 0L)

        if (until <= System.currentTimeMillis()) {
            if (until != 0L) {
                preferences.edit()
                    .remove(KEY_LOCKOUT_UNTIL)
                    .putInt(KEY_FAILED_ATTEMPTS, 0)
                    .apply()
            }
            return false
        }

        return true
    }

    fun getRecoveryLockoutRemainingMillis(): Long {
        return (
                preferences.getLong(KEY_LOCKOUT_UNTIL, 0L) -
                        System.currentTimeMillis()
                ).coerceAtLeast(0L)
    }

    /**
     * Both answers must match. Recovery is temporarily locked after
     * five failed attempts.
     */
    @Synchronized
    fun verifyRecoveryAnswers(
        firstAnswer: String,
        secondAnswer: String
    ): Boolean {
        if (!isRecoveryConfigured() || isRecoveryLockedOut()) {
            return false
        }

        val storedAnswer1 =
            preferences.getString(KEY_FIRST_ANSWER, null)
        val storedAnswer2 =
            preferences.getString(KEY_SECOND_ANSWER, null)

        if (
            storedAnswer1.isNullOrEmpty() ||
            storedAnswer2.isNullOrEmpty()
        ) {
            recordRecoveryFailure()
            return false
        }

        return try {
            val decryptedAnswer1 = decrypt(storedAnswer1)
            val decryptedAnswer2 = decrypt(storedAnswer2)

            val answersMatch =
                normalizeAnswer(decryptedAnswer1).equals(
                    normalizeAnswer(firstAnswer),
                    ignoreCase = true
                ) &&
                        normalizeAnswer(decryptedAnswer2).equals(
                            normalizeAnswer(secondAnswer),
                            ignoreCase = true
                        )

            if (answersMatch) {
                preferences.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, 0)
                    .remove(KEY_LOCKOUT_UNTIL)
                    .commit()
                true
            } else {
                recordRecoveryFailure()
                false
            }
        } catch (_: Exception) {
            recordRecoveryFailure()
            false
        }
    }

    @Synchronized
    fun clearRecoveryQuestions() {
        preferences.edit()
            .remove(KEY_FIRST_QUESTION)
            .remove(KEY_FIRST_ANSWER)
            .remove(KEY_SECOND_QUESTION)
            .remove(KEY_SECOND_ANSWER)
            .remove(KEY_RECOVERY_CONFIGURED)
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_UNTIL)
            .commit()
    }

    private fun normalizeAnswer(value: String): String {
        return value.trim().replace(Regex("\\s+"), " ")
    }

    private fun recordRecoveryFailure() {
        val attempts =
            preferences.getInt(KEY_FAILED_ATTEMPTS, 0) + 1

        val editor = preferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, attempts)

        if (attempts >= RECOVERY_LOCKOUT_AFTER_FAILURES) {
            editor.putLong(
                KEY_LOCKOUT_UNTIL,
                System.currentTimeMillis() +
                        RECOVERY_LOCKOUT_DURATION_MILLIS
            )
        }

        editor.commit()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encryptedBytes =
            cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))

        val encodedIv =
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encodedData =
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        return "$encodedIv$ENCRYPTED_DATA_SEPARATOR$encodedData"
    }

    private fun decrypt(encryptedValue: String): String {
        val parts = encryptedValue.split(ENCRYPTED_DATA_SEPARATOR)

        require(parts.size == 2) {
            "Invalid encrypted value"
        }

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator =
            KeyGenerator.getInstance(AES_ALGORITHM, ANDROID_KEYSTORE)

        val keySpec =
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    android.security.keystore.KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val PREFERENCES_NAME =
            "smart_silence_security_recovery"

        private const val KEY_FIRST_QUESTION = "first_question"
        private const val KEY_FIRST_ANSWER = "first_answer"
        private const val KEY_SECOND_QUESTION = "second_question"
        private const val KEY_SECOND_ANSWER = "second_answer"
        private const val KEY_RECOVERY_CONFIGURED = "recovery_configured"

        private const val KEY_FAILED_ATTEMPTS = "recovery_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "recovery_lockout_until"

        private const val RECOVERY_LOCKOUT_AFTER_FAILURES = 5
        private const val RECOVERY_LOCKOUT_DURATION_MILLIS = 30_000L

        private const val KEY_ALIAS = "SmartSilenceSecurityRecoveryKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val ENCRYPTED_DATA_SEPARATOR = ":"
    }
}