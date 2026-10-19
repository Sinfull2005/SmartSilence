package com.example.smartsilence.security

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DndBehaviorRecord(
    val timestamp: Long,
    val trustScore: Int,
    val trustLevel: ContextTrustLevel,
    val decision: String
)

class DndBehaviorHistory(context: Context) {

    companion object {

        private const val PREFS_NAME =
            "smart_silence_dnd_behavior_history"

        private const val KEY_HISTORY =
            "history"

        private const val MAX_HISTORY_SIZE =
            50

        const val DECISION_AUTO_ENABLED =
            "AUTO_ENABLED"

        const val DECISION_AUTO_DISABLED =
            "AUTO_DISABLED"

        const val DECISION_USER_PROMPTED =
            "USER_PROMPTED"

        const val DECISION_BLOCKED =
            "BLOCKED"

        const val DECISION_USER_ENABLED =
            "USER_ENABLED"

        const val DECISION_USER_KEPT_NORMAL =
            "USER_KEPT_NORMAL"
    }

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun record(
        trustScore: Int,
        trustLevel: ContextTrustLevel,
        decision: String
    ) {

        val history =
            getHistory().toMutableList()

        history.add(
            DndBehaviorRecord(
                timestamp = System.currentTimeMillis(),
                trustScore = trustScore.coerceIn(0, 100),
                trustLevel = trustLevel,
                decision = decision
            )
        )

        val trimmedHistory =
            if (history.size > MAX_HISTORY_SIZE) {
                history.takeLast(MAX_HISTORY_SIZE)
            } else {
                history
            }

        val jsonArray =
            JSONArray()

        trimmedHistory.forEach { record ->

            jsonArray.put(
                JSONObject().apply {

                    put(
                        "timestamp",
                        record.timestamp
                    )

                    put(
                        "trustScore",
                        record.trustScore
                    )

                    put(
                        "trustLevel",
                        record.trustLevel.name
                    )

                    put(
                        "decision",
                        record.decision
                    )
                }
            )
        }

        preferences
            .edit()
            .putString(
                KEY_HISTORY,
                jsonArray.toString()
            )
            .apply()
    }

    fun getHistory(): List<DndBehaviorRecord> {

        val storedHistory =
            preferences.getString(
                KEY_HISTORY,
                null
            )
                ?: return emptyList()

        return try {

            val jsonArray =
                JSONArray(storedHistory)

            val records =
                mutableListOf<DndBehaviorRecord>()

            for (
            index in
            0 until jsonArray.length()
            ) {

                val item =
                    jsonArray.getJSONObject(index)

                val trustLevel =
                    runCatching {

                        ContextTrustLevel.valueOf(
                            item.optString(
                                "trustLevel",
                                ContextTrustLevel.LOW.name
                            )
                        )

                    }.getOrDefault(
                        ContextTrustLevel.LOW
                    )

                records.add(
                    DndBehaviorRecord(
                        timestamp =
                            item.optLong(
                                "timestamp"
                            ),

                        trustScore =
                            item.optInt(
                                "trustScore"
                            ).coerceIn(0, 100),

                        trustLevel =
                            trustLevel,

                        decision =
                            item.optString(
                                "decision",
                                DECISION_BLOCKED
                            )
                    )
                )
            }

            records

        } catch (
            exception: Exception
        ) {

            emptyList()
        }
    }

    fun getRecentHistory(
        limit: Int = 10
    ): List<DndBehaviorRecord> {

        return getHistory()
            .takeLast(
                limit.coerceAtLeast(1)
            )
    }

    fun clearHistory() {

        preferences
            .edit()
            .remove(KEY_HISTORY)
            .apply()
    }
}