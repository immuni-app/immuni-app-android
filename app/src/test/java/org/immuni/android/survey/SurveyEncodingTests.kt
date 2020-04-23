package org.immuni.android.survey

import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.base.utils.toJson
import org.immuni.android.models.survey.*
import org.immuni.android.models.survey.raw.*
import org.junit.Assert.*
import org.junit.Test

class SurveyEncodingTests {
    @Test
    fun testSurveyWidgetCodingIsBidirectional() {
        val widgets: List<RawQuestionWidget> = listOf(
            RawQuestionWidget(
                type = RawQuestionWidgetType.MULTIPLE_CHOICES,
                answers = listOf("1", "2", "3"),
                minNumberOfAnswers = 2,
                maxNumberOfAnswers = 5
            ),
            RawQuestionWidget(
                type = RawQuestionWidgetType.PICKER,
                components = listOf(
                    listOf("1", "2"),
                    listOf("3", "4")
                )
            ),
            RawQuestionWidget(
                type = RawQuestionWidgetType.RADIO,
                answers = listOf("1", "2")
            )
        )

        try {
            for (widget in widgets) {
                val encoded = toJson(widget)
                val decoded: RawQuestionWidget = fromJson(encoded)!!

                assertEquals(widget, decoded)
            }
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testSurveyConditionCodingIsBidirectional() {
        val trueCondition = RawCondition(
            questionId = "",
            type = RawConditionType.TRUE
        )

        val conditions: List<RawCondition> = listOf(
            trueCondition,
            RawCondition(
                type = RawConditionType.FALSE
            ),
            RawCondition(
                type = RawConditionType.NOT,
                condition = trueCondition
            ),
            RawCondition(
                type = RawConditionType.AND,
                conditions = listOf()
            ),
            RawCondition(
                type = RawConditionType.AND,
                conditions = listOf(trueCondition)
            ),
            RawCondition(
                type = RawConditionType.OR,
                conditions = listOf()
            ),
            RawCondition(
                type = RawConditionType.OR,
                conditions = listOf(trueCondition)
            ),
            RawCondition(
                questionId = "test",
                type = RawConditionType.SIMPLE,
                matchingIndexes = listOf(1, 2, 3)
            ),
            RawCondition(
                questionId = "test2",
                type = RawConditionType.COMPOSITE,
                matchingComponentIndexes = listOf(
                    listOf(1, 2),
                    listOf(3, null),
                    listOf(null, null)
                )
            ),
            RawCondition(
                type = RawConditionType.CURRENT_USER_TRIAGE_PROFILE,
                matchingProfiles = listOf(
                    "covidPositive",
                    "healthyNoEstablishedContact"
                )
            ),
            RawCondition(
                type = RawConditionType.COMPOSITE,
                matchingStates = listOf("state1")
            )
        )

        try {
            for (condition in conditions) {
                val encoded = toJson(condition)
                val decoded: RawCondition = fromJson(encoded)!!

                assertEquals(conditions, decoded)
            }
        } catch (error: java.lang.Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testSurveyAnswerValueCodingIsBidirectional() {
        val answers: SurveyAnswers = mapOf(
            "" to listOf(SimpleAnswer(index = 1)),
            "" to listOf(
                SimpleAnswer(index = 1),
                SimpleAnswer(index = 2),
                SimpleAnswer(index = 3)
            ),
            "" to listOf(CompositeAnswer(componentIndexes = listOf(1, 2)))
        )

        val rawAnswers = answers.mapValues {
            it.value.map { answer ->
                when (answer) {
                    is SimpleAnswer -> answer.index
                    is CompositeAnswer -> answer.componentIndexes
                }
            }
        }

        try {
            val encoded = toJson(rawAnswers)
            val decoded: Map<QuestionId, Any> = fromJson(encoded)!!

            assertEquals(rawAnswers, decoded)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testSurveyConditionalJumperCodingIsBidirectional() {
        val trueCondition = RawCondition(
            questionId = "",
            type = RawConditionType.TRUE
        )

        val jumps: List<RawJumpItem> = listOf(
            RawJumpItem(destination = RawJumpItem.END_OF_SURVEY, condition = trueCondition),
            RawJumpItem(destination = "asd", condition = trueCondition)
        )

        try {
            for (jump in jumps) {
                val encoded = toJson(jump)
                val decoded: RawJumpItem = fromJson(encoded)!!

                assertEquals(jump, decoded)
            }
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }
}
