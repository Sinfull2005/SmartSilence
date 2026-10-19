package com.example.smartsilence.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextTrustCalculatorTest {

    private val calculator = ContextTrustCalculator()
    private val policy = ContextTrustDecisionPolicy()

    @Test
    fun highTrustShouldAutomaticallyEnableDnd() {
        val context = ContextTrustState(
            insideSavedLocation = true,
            locationRisk = 0,
            activity = "STILL",
            hourOfDay = 12,
            deviceUnlocked = true,
            behaviourDeviation = 0f,
            rapidRepeatedAction = false,
            failedAttempts = 0
        )

        val trustScore = calculator.calculate(context)
        val decision = policy.evaluate(trustScore)

        assertTrue(trustScore >= 80)
        assertEquals(ContextTrustLevel.HIGH, decision.trustLevel)
        assertTrue(decision.shouldAutoApplyDnd)
        assertFalse(decision.shouldAskUser)
        assertFalse(decision.shouldBlockDnd)
    }

    @Test
    fun mediumTrustShouldAskUser() {
        val context = ContextTrustState(
            insideSavedLocation = true,
            locationRisk = 60,
            activity = "UNKNOWN",
            hourOfDay = 20,
            deviceUnlocked = true,
            behaviourDeviation = 0f,
            rapidRepeatedAction = false,
            failedAttempts = 0
        )

        val trustScore = calculator.calculate(context)
        val decision = policy.evaluate(trustScore)

        assertTrue(trustScore in 50..79)
        assertEquals(ContextTrustLevel.MEDIUM, decision.trustLevel)
        assertFalse(decision.shouldAutoApplyDnd)
        assertTrue(decision.shouldAskUser)
        assertFalse(decision.shouldBlockDnd)
    }

    @Test
    fun lowTrustShouldBlockDnd() {
        val context = ContextTrustState(
            insideSavedLocation = false,
            locationRisk = 100,
            activity = "UNKNOWN",
            hourOfDay = 2,
            deviceUnlocked = false,
            behaviourDeviation = 1f,
            rapidRepeatedAction = true,
            failedAttempts = 5
        )

        val trustScore = calculator.calculate(context)
        val decision = policy.evaluate(trustScore)

        assertTrue(trustScore < 50)
        assertEquals(ContextTrustLevel.LOW, decision.trustLevel)
        assertFalse(decision.shouldAutoApplyDnd)
        assertFalse(decision.shouldAskUser)
        assertTrue(decision.shouldBlockDnd)
    }
}