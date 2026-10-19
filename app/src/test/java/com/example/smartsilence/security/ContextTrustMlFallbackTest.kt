package com.example.smartsilence.security

import org.junit.Assert.assertTrue
import org.junit.Test

class ContextTrustMlFallbackTest {

    @Test
    fun ruleBasedTrustCalculationRemainsValidWithoutMlModel() {

        val context =
            ContextTrustState(
                insideSavedLocation = true,
                locationRisk = 0,
                activity = "STILL",
                hourOfDay = 12,
                deviceUnlocked = true,
                behaviourDeviation = 0f,
                rapidRepeatedAction = false,
                failedAttempts = 0
            )

        val ruleCalculator =
            ContextTrustCalculator()

        val ruleScore =
            ruleCalculator.calculate(context)

        assertTrue(
            "Rule-based trust score should remain within 0-100.",
            ruleScore in 0..100
        )
    }
}