package com.example.smartsilence.security

import android.content.Context

class SuspiciousMovementDetector(
    context: Context
) {

    companion object {

        private const val PREFS_NAME =
            "smart_silence_movement_detection"

        private const val KEY_TRANSITIONS =
            "recent_transitions"

        private const val KEY_LAST_ENTER_PREFIX =
            "last_enter_"

        private const val KEY_LAST_EXIT_PREFIX =
            "last_exit_"

        /*
         * Entering and leaving the same saved location
         * within this period is considered unusually fast.
         */
        private const val RAPID_SAME_LOCATION_WINDOW_MS =
            60_000L

        /*
         * More than this many transitions inside the window
         * indicates repeated movement/context changes.
         */
        private const val MAX_TRANSITIONS =
            6

        private const val TRANSITION_WINDOW_MS =
            5 * 60_000L
    }

    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun recordEnter(
        locationId: String
    ): Boolean {

        val now =
            System.currentTimeMillis()

        val recentTransitions =
            getRecentTransitions(now)
                .toMutableList()

        recentTransitions.add(
            "$now|ENTER|$locationId"
        )

        saveTransitions(
            recentTransitions
        )

        val lastExit =
            preferences.getLong(
                KEY_LAST_EXIT_PREFIX + locationId,
                0L
            )

        val rapidSameLocation =
            lastExit > 0L &&
                    now - lastExit <=
                    RAPID_SAME_LOCATION_WINDOW_MS

        val tooManyTransitions =
            recentTransitions.size >
                    MAX_TRANSITIONS

        preferences
            .edit()
            .putLong(
                KEY_LAST_ENTER_PREFIX + locationId,
                now
            )
            .apply()

        return rapidSameLocation ||
                tooManyTransitions
    }

    @Synchronized
    fun recordExit(
        locationId: String
    ): Boolean {

        val now =
            System.currentTimeMillis()

        val recentTransitions =
            getRecentTransitions(now)
                .toMutableList()

        recentTransitions.add(
            "$now|EXIT|$locationId"
        )

        saveTransitions(
            recentTransitions
        )

        val lastEnter =
            preferences.getLong(
                KEY_LAST_ENTER_PREFIX + locationId,
                0L
            )

        val rapidSameLocation =
            lastEnter > 0L &&
                    now - lastEnter <=
                    RAPID_SAME_LOCATION_WINDOW_MS

        val tooManyTransitions =
            recentTransitions.size >
                    MAX_TRANSITIONS

        preferences
            .edit()
            .putLong(
                KEY_LAST_EXIT_PREFIX + locationId,
                now
            )
            .apply()

        return rapidSameLocation ||
                tooManyTransitions
    }

    @Synchronized
    fun isRapidMovement(
        locationId: String
    ): Boolean {

        val now =
            System.currentTimeMillis()

        val lastEnter =
            preferences.getLong(
                KEY_LAST_ENTER_PREFIX + locationId,
                0L
            )

        val lastExit =
            preferences.getLong(
                KEY_LAST_EXIT_PREFIX + locationId,
                0L
            )

        val recentEnter =
            lastEnter > 0L &&
                    now - lastEnter <=
                    RAPID_SAME_LOCATION_WINDOW_MS

        val recentExit =
            lastExit > 0L &&
                    now - lastExit <=
                    RAPID_SAME_LOCATION_WINDOW_MS

        return recentEnter || recentExit
    }

    @Synchronized
    fun getRecentTransitionCount(): Int {

        return getRecentTransitions(
            System.currentTimeMillis()
        ).size
    }

    @Synchronized
    fun clearHistory() {

        preferences
            .edit()
            .clear()
            .apply()
    }

    private fun getRecentTransitions(
        now: Long
    ): List<String> {

        val stored =
            preferences.getStringSet(
                KEY_TRANSITIONS,
                emptySet()
            )
                ?: emptySet()

        val recent =
            stored.filter { entry ->

                val timestamp =
                    entry.substringBefore("|")
                        .toLongOrNull()
                        ?: return@filter false

                now - timestamp <=
                        TRANSITION_WINDOW_MS
            }

        return recent
    }

    private fun saveTransitions(
        transitions: List<String>
    ) {

        val now =
            System.currentTimeMillis()

        val cleaned =
            transitions
                .filter { entry ->

                    val timestamp =
                        entry.substringBefore("|")
                            .toLongOrNull()
                            ?: return@filter false

                    now - timestamp <=
                            TRANSITION_WINDOW_MS
                }
                .takeLast(
                    MAX_TRANSITIONS + 1
                )
                .toSet()

        preferences
            .edit()
            .putStringSet(
                KEY_TRANSITIONS,
                cleaned
            )
            .apply()
    }
}