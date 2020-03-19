package com.bendingspoons.ascolto

import com.bendingspoons.ascolto.models.survey.*
import org.junit.Test

import org.junit.Assert.*

class SurveyUnitTests {
    @Test
    fun `survey conditions work`() {
        val question1 = Question(
            id = "question1",
            title = "Come stai?",
            description = "Dimmi se stai bene o male",
            frequency = 0,
            widgetInfo = mapOf(
                "type" to WidgetType.RADIO.id,
                "answers" to listOf(
                    "Bene",
                    "Male"
                )
            )
        )

        val isntFeelingWell = RawConditionItem(
            questionId = question1.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.SIMPLE.id,
                "matching_indexes" to listOf(
                    (question1.widget as RadioWidget).answers.indexOf("Male")
                )
            )
        )

        val question2 = Question(
            id = "question2",
            title = "Che sintomi hai?",
            description = "Dimmi che sintomi hai",
            frequency = 0,
            showConditions = listOf(
                isntFeelingWell
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.MULTIPLE_CHOICES.id,
                "answers" to listOf(
                    "Mal di testa",
                    "Febbre",
                    "Nausea"
                )
            )
        )

        val hasNausea = RawConditionItem(
            questionId = question2.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.SIMPLE.id,
                "matching_indexes" to listOf(
                    (question2.widget as MultipleChoicesWidget).answers.indexOf("Nausea")
                )
            )
        )
        val hasFever = RawConditionItem(
            questionId = question2.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.SIMPLE.id,
                "matching_indexes" to listOf(
                    (question2.widget as MultipleChoicesWidget).answers.indexOf("Febbre")
                )
            )
        )
        val hasHeadache = RawConditionItem(
            questionId = question2.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.SIMPLE.id,
                "matching_indexes" to listOf(
                    (question2.widget as MultipleChoicesWidget).answers.indexOf("Mal di testa")
                )
            )
        )

        val question3 = Question(
            id = "question3",
            title = "Quanta febbre hai?",
            description = "Seleziona la temperatura",
            frequency = 0,
            showConditions = listOf(
                hasFever
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.PICKER.id,
                "components" to listOf(
                    listOf(
                        "35",
                        "36",
                        "37",
                        "38",
                        "39",
                        "40",
                        "41",
                        "42"
                    ),
                    listOf(
                        ".0",
                        ".1",
                        ".2",
                        ".3",
                        ".4",
                        ".5",
                        ".6",
                        ".7",
                        ".8",
                        ".9"
                    )
                )
            )
        )

        val has37OfFever = RawConditionItem(
            questionId = question3.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.COMPOSITE.id,
                "matching_component_indexes" to listOf(
                    listOf(
                        (question3.widget as PickerWidget).components.first().indexOf("37"),
                        null
                    )
                )
            )
        )
        val has38OfFever = RawConditionItem(
            questionId = question3.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.COMPOSITE.id,
                "matching_component_indexes" to listOf(
                    listOf(
                        (question3.widget as PickerWidget).components.first().indexOf("38"),
                        null
                    )
                )
            )
        )
        val has37or38OfFever = RawConditionItem(
            questionId = question3.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.COMPOSITE.id,
                "matching_component_indexes" to listOf(
                    listOf(
                        (question3.widget as PickerWidget).components[0].indexOf("37"),
                        null
                    ),
                    listOf(
                        (question3.widget as PickerWidget).components[0].indexOf("38"),
                        null
                    )
                )
            )
        )
        val hasExactly38Point5OfFever = RawConditionItem(
            questionId = question3.id,
            predicate = mapOf(
                "type" to ConditionPredicateType.COMPOSITE.id,
                "matching_component_indexes" to listOf(
                    listOf(
                        (question3.widget as PickerWidget).components[0].indexOf("38"),
                        (question3.widget as PickerWidget).components[1].indexOf(".5")
                    )
                )
            )
        )


        val question4 = Question(
            id = "question4",
            title = "Hai mai avuto febbre così alta?",
            description = "Sì o no",
            frequency = 0,
            showConditions = listOf(
                has38OfFever
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.RADIO.id,
                "answers" to listOf(
                    "Sì",
                    "No"
                )
            )
        )

        val question5 = Question(
            id = "question5",
            title = "Hai mai avuto febbre così alta?",
            description = "Sì o no",
            frequency = 0,
            showConditions = listOf(
                has37OfFever
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.RADIO.id,
                "answers" to listOf(
                    "Sì",
                    "No"
                )
            )
        )

        val question6 = Question(
            id = "question6",
            title = "Hai mai avuto febbre così alta?",
            description = "Sì o no",
            frequency = 0,
            showConditions = listOf(
                has37or38OfFever
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.RADIO.id,
                "answers" to listOf(
                    "Sì",
                    "No"
                )
            )
        )

        val question7 = Question(
            id = "question7",
            title = "Hai mai avuto esattamente 38.5 di febbre?",
            description = "Sì o no",
            frequency = 0,
            showConditions = listOf(
                hasExactly38Point5OfFever
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.RADIO.id,
                "answers" to listOf(
                    "Sì",
                    "No"
                )
            )
        )

        val question8 = Question(
            id = "question7",
            title = "Hai mai avuto sia febbre che nausea?",
            description = "Sì o no",
            frequency = 0,
            showConditions = listOf(
                has37or38OfFever,
                hasNausea
            ),
            stopSurveyCondition = null,
            widgetInfo = mapOf(
                "type" to WidgetType.RADIO.id,
                "answers" to listOf(
                    "Sì",
                    "No"
                )
            )
        )

        val survey = Survey(
            id = "test",
            questions = listOf(
                question1,
                question2,
                question3,
                question4,
                question5,
                question6,
                question7,
                question8

            ),
            triage = Triage(
                mapOf(
                    HealthStatus.INFECTED to Condition(
                        listOf(
                            isntFeelingWell.conditionItem,
                            has38OfFever.conditionItem
                        )
                    )
                )
            )
        )

        val answers = mapOf(
            question1.id to listOf(
                SimpleAnswer(1)
            ),
            question2.id to listOf(
                SimpleAnswer(1),
                SimpleAnswer(2)
            ),
            question3.id to listOf(
                CompositeAnswer(
                    listOf(
                        (question3.widget as PickerWidget).components[0].indexOf("38"),
                        (question3.widget as PickerWidget).components[1].indexOf(".5")
                    )
                )
            )
        )

        assertTrue("Should show question 2", question2.shouldBeShown(answers))
        assertTrue("Should show question 3", question3.shouldBeShown(answers))
        assertTrue("Should show question 4", question4.shouldBeShown(answers))
        assertFalse("Should not show question 5", question5.shouldBeShown(answers))
        assertTrue("Should not show question 6", question6.shouldBeShown(answers))
        assertTrue("Should not show question 7", question7.shouldBeShown(answers))
        assertTrue("Should not show question 8", question8.shouldBeShown(answers))

        assertTrue("Is infected", survey.triage(HealthStatus.INFECTED, answers))
    }
}
