package com.example.smartsilence.security

import android.content.Context
import org.json.JSONArray

/**
 * Tracks a small history of protected Smart Silence
 * actions for behavioural-deviation analysis.
 *
 * Only action identifiers and timestamps are stored.
 * No credentials or personal content are stored.
 */
class SecurityBehaviourTracker(
    context: Context
) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    /**
     * Records a protected action and returns the
     * behavioural deviation associated with it.
     *
     * Result:
     *
     * 0.0 -> normal/familiar behaviour
     * 1.0 -> highly unusual behaviour
     */
    @Synchronized
    fun recordAction(
        action: SensitiveAction
    ): Float {

        val history =
            getHistory()

        val deviation =
            calculateDeviation(
                action = action,
                history = history
            )

        history.put(
            org.json.JSONObject().apply {

                put(
                    "action",
                    action.name
                )

                put(
                    "timestamp",
                    System.currentTimeMillis()
                )
            }
        )

        /*
         * Keep only the most recent actions.
         * This prevents unlimited growth.
         */
        while (
            history.length() >
            MAX_HISTORY_SIZE
        ) {
            history.remove(0)
        }

        saveHistory(history)

        return deviation
    }

    /**
     * Clears behavioural history.
     *
     * This is intentionally separate from the
     * security event log.
     */
    @Synchronized
    fun clearHistory() {

        preferences.edit()
            .remove(
                KEY_ACTION_HISTORY
            )
            .apply()
    }

    /**
     * Calculates how unusual the current action is
     * compared with the recent action history.
     *
     * The first few actions establish a baseline and
     * therefore do not produce an exaggerated anomaly.
     */
    private fun calculateDeviation(
        action: SensitiveAction,
        history: JSONArray
    ): Float {

        val historySize =
            history.length()

        if (
            historySize <
            MIN_HISTORY_FOR_DEVIATION
        ) {
            return 0f
        }

        var matchingActions = 0

        for (
        index in 0 until historySize
        ) {

            val item =
                history.optJSONObject(
                    index
                ) ?: continue

            if (
                item.optString(
                    "action"
                ) == action.name
            ) {
                matchingActions++
            }
        }

        /*
         * An action that is frequently seen is considered
         * normal. A rare/new action is considered more
         * behaviourally unusual.
         */
        val familiarity =
            matchingActions.toFloat() /
                    historySize.toFloat()

        return (
                1f - familiarity
                ).coerceIn(
                0f,
                1f
            )
    }

    private fun getHistory(): JSONArray {

        val stored =
            preferences.getString(
                KEY_ACTION_HISTORY,
                null
            )

        if (
            stored.isNullOrEmpty()
        ) {
            return JSONArray()
        }

        return try {
            JSONArray(stored)
        } catch (
            exception: Exception
        ) {
            JSONArray()
        }
    }

    private fun saveHistory(
        history: JSONArray
    ) {

        preferences.edit()
            .putString(
                KEY_ACTION_HISTORY,
                history.toString()
            )
            .apply()
    }

    companion object {

        private const val PREFERENCES_NAME =
            "smart_silence_security_behaviour"

        private const val KEY_ACTION_HISTORY =
            "protected_action_history"

        private const val MAX_HISTORY_SIZE =
            50

        private const val MIN_HISTORY_FOR_DEVIATION =
            5
    }
}