package com.example.smartsilence.security

import org.junit.Assert.assertEquals
import org.junit.Test

class ContextTrustMlCombinerTest {

    private val combiner =
        ContextTrustMlCombiner()

    @Test
    fun mlUnavailableKeepsRuleScore() {

        val result =
            combiner.combine(
                ruleTrustScore = 75,
                mlBehaviourScore = null
            )

        assertEquals(
            75,
            result
        )
    }

    @Test
    fun mlCanReduceTrust() {

        val mlScore =
            MlBehaviourScore(
                mlRiskScore = 70,
                behaviourScore = 30
            )

        val result =
            combiner.combine(
                ruleTrustScore = 90,
                mlBehaviourScore = mlScore
            )

        assertEquals(
            72,
            result
        )
    }

    @Test
    fun mlCannotIncreaseTrust() {

        val mlScore =
            MlBehaviourScore(
                mlRiskScore = 0,
                behaviourScore = 100
            )

        val result =
            combiner.combine(
                ruleTrustScore = 60,
                mlBehaviourScore = mlScore
            )

        assertEquals(
            60,
            result
        )
    }

    @Test
    fun zeroScoresRemainZero() {

        val mlScore =
            MlBehaviourScore(
                mlRiskScore = 100,
                behaviourScore = 0
            )

        val result =
            combiner.combine(
                ruleTrustScore = 0,
                mlBehaviourScore = mlScore
            )

        assertEquals(
            0,
            result
        )
    }

    @Test
    fun maximumRuleScoreCannotExceed100() {

        val mlScore =
            MlBehaviourScore(
                mlRiskScore = 0,
                behaviourScore = 100
            )

        val result =
            combiner.combine(
                ruleTrustScore = 100,
                mlBehaviourScore = mlScore
            )

        assertEquals(
            100,
            result
        )
    }
}