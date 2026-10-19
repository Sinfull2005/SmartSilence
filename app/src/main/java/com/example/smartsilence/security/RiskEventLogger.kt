
package com.example.smartsilence.security

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores Risk & ML assessment events independently
 * from the regular Security Event Log.
 */
class RiskEventLogger(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun logEvent(
        eventType: String,
        action: String? = null,
        details: String? = null
    ) {
        val event = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("eventType", eventType)
            if (action != null) put("action", action)
            if (details != null) put("details", details)
        }

        val events = getEventsAsJson()
        events.put(event)

        while (events.length() > MAX_EVENTS) {
            events.remove(0)
        }

        saveEvents(events)
    }

    @Synchronized
    fun getEvents(): List<SecurityEvent> {
        val events = getEventsAsJson()
        val result = mutableListOf<SecurityEvent>()

        for (index in 0 until events.length()) {
            val event = events.optJSONObject(index) ?: continue

            result.add(
                SecurityEvent(
                    timestamp = event.optLong("timestamp"),
                    eventType = event.optString("eventType"),
                    action = event.optString("action")
                        .takeIf { it.isNotEmpty() },
                    details = event.optString("details")
                        .takeIf { it.isNotEmpty() }
                )
            )
        }

        return result
    }

    /**
     * Clears only the Risk & ML log.
     * The regular Security Event Log is untouched.
     */
    @Synchronized
    fun clearEvents() {
        preferences.edit()
            .remove(KEY_ENCRYPTED_EVENTS)
            .apply()
    }

    private fun getEventsAsJson(): JSONArray {
        val encryptedData = preferences.getString(
            KEY_ENCRYPTED_EVENTS,
            null
        )

        if (encryptedData.isNullOrEmpty()) return JSONArray()

        return try {
            val parts = encryptedData.split(ENCRYPTED_DATA_SEPARATOR)
            if (parts.size != 2) return JSONArray()

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(GCM_TAG_LENGTH, iv)
            )

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            JSONArray(String(decryptedBytes, StandardCharsets.UTF_8))
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun saveEvents(events: JSONArray) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

            val encryptedBytes = cipher.doFinal(
                events.toString().toByteArray(StandardCharsets.UTF_8)
            )

            val encodedIv = Base64.encodeToString(
                cipher.iv,
                Base64.NO_WRAP
            )

            val encodedData = Base64.encodeToString(
                encryptedBytes,
                Base64.NO_WRAP
            )

            preferences.edit()
                .putString(
                    KEY_ENCRYPTED_EVENTS,
                    "$encodedIv$ENCRYPTED_DATA_SEPARATOR$encodedData"
                )
                .apply()
        } catch (_: Exception) {
            // Logging must never crash the main application.
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey is SecretKey) return existingKey

        val keyGenerator = KeyGenerator.getInstance(
            AES_ALGORITHM,
            ANDROID_KEYSTORE
        )

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
            "smart_silence_risk_event_log"

        private const val KEY_ENCRYPTED_EVENTS =
            "encrypted_risk_events"

        private const val KEY_ALIAS =
            "SmartSilenceRiskEventKey"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val MAX_EVENTS = 100
        private const val ENCRYPTED_DATA_SEPARATOR = ":"
    }
}
