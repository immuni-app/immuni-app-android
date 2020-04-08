package org.immuni.android

import org.immuni.android.models.survey.*
import org.junit.Test
import org.junit.Assert.*

class SurveyConditionTests {
    @Test
    fun `conditions never match with empty input`() {
        val conditions: List<Condition> = listOf(
            SimpleCondition(questionId = "test", matchingIndexes = listOf(1, 2, 3)),
            SimpleCondition(questionId = "test", matchingIndexes = listOf()),
            CompositeCondition(questionId = "test2", matchingComponentIndexes = listOf(listOf())),
            CompositeCondition(
                questionId = "test2",
                matchingComponentIndexes = listOf(listOf(1, 2), listOf(3, 4))
            ),
            TriageProfileCondition(matchingProfiles = listOf()),
            TriageProfileCondition(matchingProfiles = listOf("profile", "profile2")),
            StatesContainCondition(matchingStates = setOf()),
            StatesContainCondition(matchingStates = setOf("state"))
        )

        for (condition in conditions) {
            val isSatisfied = condition.isSatisfied(
                healthState = setOf(),
                triageProfile = null,
                surveyAnswers = mapOf()
            )

            assertFalse("Condition $condition matches", isSatisfied)
        }
    }

    @Test
    fun `value conditions dont match when question id is not present`() {
        val conditions: List<Condition> = listOf(
            SimpleCondition(questionId = "test", matchingIndexes = listOf(1, 2, 3)),
            CompositeCondition(
                questionId = "test2",
                matchingComponentIndexes = listOf(listOf(1, 2), listOf(3, 4))
            )
        )

        val answers: SurveyAnswers = mapOf(
            "test3" to listOf(SimpleAnswer(index = 1)),
            "test4" to listOf(CompositeAnswer(componentIndexes = listOf(1, 2))),
            "test5" to listOf(SimpleAnswer(index = 1))
        )

        for (condition in conditions) {
            val isSatisfied = condition.isSatisfied(
                surveyAnswers = answers,
                healthState = setOf(),
                triageProfile = null
            )

            assertFalse("Condition $condition matches", isSatisfied)
        }
    }

    @Test
    fun `simple condition matches when intersection is not empty`() {
        val condition = SimpleCondition(questionId = "id", matchingIndexes = listOf(1, 2, 3))

        val answersList = listOf(
            listOf(SimpleAnswer(index = 1)),
            listOf(
                SimpleAnswer(index = 2),
                SimpleAnswer(index = 5)
            )
        )
        for (answerList in answersList) {
            val answers: SurveyAnswers = mapOf("id" to answerList)

            val isSatisfied = condition.isSatisfied(
                surveyAnswers = answers,
                healthState = setOf(),
                triageProfile = null
            )

            assertTrue("Condition $condition doesn't match answers $answers", isSatisfied)
        }
    }

    @Test(expected = ClassCastException::class)
    fun `simple condition doesnt match picker value`() {
        val condition = SimpleCondition(questionId = "id", matchingIndexes = listOf(1, 2, 3))
        val answers = mapOf("id" to listOf(CompositeAnswer(componentIndexes = listOf(2, 5))))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = answers,
            healthState = setOf(),
            triageProfile = null
        )
    }

    @Test
    fun `complex condition matches picker when intersection is not empty`() {
        val condition = CompositeCondition(
            questionId = "id",
            matchingComponentIndexes = listOf(
                listOf(2, 5),
                listOf(3, 6),
                listOf(1, 0)
            )
        )
        val answers = mapOf("id" to listOf(CompositeAnswer(componentIndexes = listOf(2, 5))))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = answers,
            healthState = setOf(),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `complex condition matches picker when intersection is not empty with wildcard`() {
        val condition = CompositeCondition(
            questionId = "id",
            matchingComponentIndexes = listOf(listOf(2, null))
        )
        val answers = mapOf("id" to listOf(CompositeAnswer(componentIndexes = listOf(2, 5))))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = answers,
            healthState = setOf(),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test(expected = ClassCastException::class)
    fun `complex condition does not match other value types`() {
        val condition = CompositeCondition(
            questionId = "id",
            matchingComponentIndexes = listOf(listOf(2, 5), listOf(3, 6), listOf(1, 0))
        )

        val answersList = listOf(
            listOf(SimpleAnswer(index = 2)),
            listOf(
                SimpleAnswer(index = 2),
                SimpleAnswer(index = 5)
            )
        )
        for (answerList in answersList) {
            val answers: SurveyAnswers = mapOf("id" to answerList)

            val isSatisfied = condition.isSatisfied(
                surveyAnswers = answers,
                healthState = setOf(),
                triageProfile = null
            )
        }
    }

    @Test
    fun `state contains condition matches if intersection is not empty`() {
        val condition = StatesContainCondition(matchingStates = setOf("state", "bsd"))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf("state", "asd"),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `state contains condition does not match if intersection is empty`() {
        val condition = StatesContainCondition(matchingStates = setOf("state", "bsd"))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf("asd"),
            triageProfile = null
        )
        assertFalse(isSatisfied)
    }

    @Test
    fun `triage profile condition matches if intersection is not empty`() {
        val condition = TriageProfileCondition(matchingProfiles = listOf("state", "bsd"))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = "state"
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `triage profile condition does not match if intersection is empty`() {
        val condition = TriageProfileCondition(matchingProfiles = listOf("state", null))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = "UserID"
        )
        assertFalse(isSatisfied)
    }

    @Test
    fun `triage profile condition matches if intersection is not empty with null triage profile`() {
        val condition = TriageProfileCondition(matchingProfiles = listOf("state", null))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `triage profile condition does not match if intersection is empty with null triage profile`() {
        val condition = TriageProfileCondition(matchingProfiles = listOf("state", "bsd"))

        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = null
        )
        assertFalse(isSatisfied)
    }

    @Test
    fun `and of empty array returns true`() {
        val condition = AndCondition(conditions = listOf())
        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `or of empty array returns false`() {
        val condition = OrCondition(conditions = listOf())
        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = null
        )
        assertFalse(isSatisfied)
    }

    @Test
    fun `TrueCondition returns true`() {
        val condition = TrueCondition
        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `FalseCondition returns false`() {
        val condition = TrueCondition
        val isSatisfied = condition.isSatisfied(
            surveyAnswers = mapOf(),
            healthState = setOf(),
            triageProfile = null
        )
        assertTrue(isSatisfied)
    }

    @Test
    fun `AndCondition behaves as an and`() {
        val testCases: List<Pair<List<Condition>, Boolean>> = listOf(
            Pair(listOf(), true),
            Pair(listOf(TrueCondition), true),
            Pair(listOf(FalseCondition), false),
            Pair(listOf(TrueCondition, TrueCondition), true),
            Pair(listOf(TrueCondition, FalseCondition), false),
            Pair(listOf(FalseCondition, TrueCondition), false),
            Pair(listOf(FalseCondition, FalseCondition), false)
        )

        for (testResult in testCases) {
            val (input, expectedResult) = testResult
            val condition = AndCondition(conditions = input)
            val isSatisfied = condition.isSatisfied(
                surveyAnswers = mapOf(),
                healthState = setOf(),
                triageProfile = null
            )
            assertEquals(expectedResult, isSatisfied)
        }
    }

    @Test
    fun `OrCondition behaves as an or`() {
        val testCases: List<Pair<List<Condition>, Boolean>> = listOf(
            Pair(listOf(), false),
            Pair(listOf(TrueCondition), true),
            Pair(listOf(FalseCondition), false),
            Pair(listOf(TrueCondition, TrueCondition), true),
            Pair(listOf(TrueCondition, FalseCondition), true),
            Pair(listOf(FalseCondition, TrueCondition), true),
            Pair(listOf(FalseCondition, FalseCondition), false)
        )

        for (testResult in testCases) {
            val (input, expectedResult) = testResult
            val condition = OrCondition(conditions = input)
            val isSatisfied = condition.isSatisfied(
                surveyAnswers = mapOf(),
                healthState = setOf(),
                triageProfile = null
            )
            assertEquals(expectedResult, isSatisfied)
        }
    }

    @Test
    fun `NotCondition behaves as not`() {

        val testCases: List<Pair<Condition, Boolean>> = listOf(
            Pair(TrueCondition, false),
            Pair(FalseCondition, true)
        )

        for (testResult in testCases) {
            val (input, expectedResult) = testResult
            val condition = NotCondition(condition = input)
            val isSatisfied = condition.isSatisfied(
                surveyAnswers = mapOf(),
                healthState = setOf(),
                triageProfile = null
            )
            assertEquals(expectedResult, isSatisfied)
        }
    }
}
