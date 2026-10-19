package com.example.smartsilence.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextTrustMlPipelineTest {

    private val calculator =
        ContextTrustCalculator()

    private val combiner =
        ContextTrustMlCombiner()


    @Test
    fun highTrustContextProducesHighRuleScore() {

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

        val ruleScore =
            calculator.calculate(context)

        assertTrue(
            "High-trust context should produce a high rule score.",
            ruleScore >= 80
        )
    }


    @Test
    fun suspiciousContextProducesLowerRuleScore() {

        val context =
            ContextTrustState(
                insideSavedLocation = false,
                locationRisk = 100,
                activity = "UNKNOWN",
                hourOfDay = 2,
                deviceUnlocked = false,
                behaviourDeviation = 1f,
                rapidRepeatedAction = true,
                failedAttempts = 5
            )

        val ruleScore =
            calculator.calculate(context)

        assertTrue(
            "Suspicious context should produce a low rule score.",
            ruleScore < 50
        )
    }


    @Test
    fun mlRiskCanReduceTrustScore() {

        val ruleScore = 90

        val mlBehaviourScore =
            MlBehaviourScore(
                mlRiskScore = 70,
                behaviourScore = 30
            )

        val finalScore =
            combiner.combine(
                ruleTrustScore = ruleScore,
                mlBehaviourScore = mlBehaviourScore
            )

        assertEquals(
            72,
            finalScore
        )

        assertTrue(
            "ML risk should not increase the rule score.",
            finalScore <= ruleScore
        )
    }


    @Test
    fun mlCannotIncreaseTrustScore() {

        val ruleScore = 60

        val mlBehaviourScore =
            MlBehaviourScore(
                mlRiskScore = 0,
                behaviourScore = 100
            )

        val finalScore =
            combiner.combine(
                ruleTrustScore = ruleScore,
                mlBehaviourScore = mlBehaviourScore
            )

        assertEquals(
            60,
            finalScore
        )
    }


    @Test
    fun unavailableMlFallsBackToRuleScore() {

        val ruleScore = 75

        val finalScore =
            combiner.combine(
                ruleTrustScore = ruleScore,
                mlBehaviourScore = null
            )

        assertEquals(
            ruleScore,
            finalScore
        )
    }


    @Test
    fun combinedScoreAlwaysRemainsWithinValidRange() {

        val testScores =
            listOf(
                0,
                10,
                25,
                50,
                75,
                90,
                100
            )

        testScores.forEach { ruleScore ->

            val mlBehaviourScore =
                MlBehaviourScore(
                    mlRiskScore = 50,
                    behaviourScore = 50
                )

            val finalScore =
                combiner.combine(
                    ruleTrustScore = ruleScore,
                    mlBehaviourScore = mlBehaviourScore
                )

            assertTrue(
                "Final score must remain between 0 and 100.",
                finalScore in 0..100
            )
        }
    }
}