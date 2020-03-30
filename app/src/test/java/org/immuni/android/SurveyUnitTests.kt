package org.immuni.android

import org.immuni.android.models.survey.*
import org.immuni.android.models.survey.raw.RawSurvey
import com.squareup.moshi.Moshi
import org.junit.Test

import org.junit.Assert.*
import testSurveyJson

class SurveyUnitTests {

    @Test
    fun `survey decodes from json`() {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RawSurvey::class.java)
        val survey = adapter.fromJson(testSurveyJson)
        assertNotNull("Survey decoded from json", survey)
    }

//    @Test
//    fun `survey conditions work`() {
//        val question1 = Question(
//            id = "question1",
//            title = "Come stai?",
//            description = "Dimmi se stai bene o male",
//            frequency = 0,
//            showCondition = null,
//            stopSurveyCondition = null,
//            widget = RadioWidget(
//                answers = listOf(
//                    "Bene",
//                    "Male"
//                )
//            )
//        )
//
//        val isntFeelingWell = SimpleCondition(
//            questionId = question1.id,
//            matchingIndexes = listOf(
//                (question1.widget as RadioWidget).answers.indexOf("Male")
//            )
//        )
//
//        val question2 = Question(
//            id = "question2",
//            title = "Che sintomi hai?",
//            description = "Dimmi che sintomi hai",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    isntFeelingWell
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = MultipleChoicesWidget(
//                minNumberOfAnswers = 0,
//                maxNumberOfAnswers = 9999,
//                answers = listOf(
//                    "Mal di testa",
//                    "Febbre",
//                    "Nausea"
//                )
//            )
//        )
//
//        val hasNausea = SimpleCondition(
//            questionId = question2.id,
//            matchingIndexes = listOf(
//                (question2.widget as MultipleChoicesWidget).answers.indexOf("Nausea")
//            )
//        )
//        val hasFever = SimpleCondition(
//            questionId = question2.id,
//            matchingIndexes = listOf(
//                (question2.widget as MultipleChoicesWidget).answers.indexOf("Febbre")
//            )
//        )
//        val hasHeadache = SimpleCondition(
//            questionId = question2.id,
//            matchingIndexes = listOf(
//                (question2.widget as MultipleChoicesWidget).answers.indexOf("Mal di testa")
//            )
//        )
//
//        val question3 = Question(
//            id = "question3",
//            title = "Quanta febbre hai?",
//            description = "Seleziona la temperatura",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    hasFever
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = PickerWidget(
//                components = listOf(
//                    listOf(
//                        "35",
//                        "36",
//                        "37",
//                        "38",
//                        "39",
//                        "40",
//                        "41",
//                        "42"
//                    ),
//                    listOf(
//                        ".0",
//                        ".1",
//                        ".2",
//                        ".3",
//                        ".4",
//                        ".5",
//                        ".6",
//                        ".7",
//                        ".8",
//                        ".9"
//                    )
//                )
//            )
//        )
//
//        val has37OfFever = CompositeCondition(
//            questionId = question3.id,
//            matchingComponentIndexes = listOf(
//                listOf(
//                    (question3.widget as PickerWidget).components.first().indexOf("37"),
//                    null
//                )
//            )
//        )
//        val has38OfFever = CompositeCondition(
//            questionId = question3.id,
//            matchingComponentIndexes = listOf(
//                listOf(
//                    (question3.widget as PickerWidget).components.first().indexOf("38"),
//                    null
//                )
//            )
//        )
//        val has37or38OfFever = CompositeCondition(
//            questionId = question3.id,
//            matchingComponentIndexes = listOf(
//                listOf(
//                    (question3.widget as PickerWidget).components[0].indexOf("37"),
//                    null
//                ),
//                listOf(
//                    (question3.widget as PickerWidget).components[0].indexOf("38"),
//                    null
//                )
//            )
//        )
//        val hasExactly38Point5OfFever = CompositeCondition(
//            questionId = question3.id,
//            matchingComponentIndexes = listOf(
//                listOf(
//                    (question3.widget as PickerWidget).components[0].indexOf("38"),
//                    (question3.widget as PickerWidget).components[1].indexOf(".5")
//                )
//            )
//        )
//
//
//        val question4 = Question(
//            id = "question4",
//            title = "Hai mai avuto febbre così alta?",
//            description = "Sì o no",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    has38OfFever
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = RadioWidget(
//                answers = listOf(
//                    "Sì",
//                    "No"
//                )
//            )
//        )
//
//        val question5 = Question(
//            id = "question5",
//            title = "Hai mai avuto febbre così alta?",
//            description = "Sì o no",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    has37OfFever
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = RadioWidget(
//                answers = listOf(
//                    "Sì",
//                    "No"
//                )
//            )
//        )
//
//        val question6 = Question(
//            id = "question6",
//            title = "Hai mai avuto febbre così alta?",
//            description = "Sì o no",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    has37or38OfFever
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = RadioWidget(
//                answers = listOf(
//                    "Sì",
//                    "No"
//                )
//            )
//        )
//
//        val question7 = Question(
//            id = "question7",
//            title = "Hai mai avuto esattamente 38.5 di febbre?",
//            description = "Sì o no",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    hasExactly38Point5OfFever
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = RadioWidget(
//                answers = listOf(
//                    "Sì",
//                    "No"
//                )
//            )
//        )
//
//        val question8 = Question(
//            id = "question7",
//            title = "Hai mai avuto sia febbre che nausea?",
//            description = "Sì o no",
//            frequency = 0,
//            showCondition = Condition(
//                listOf(
//                    has37or38OfFever,
//                    hasNausea
//                )
//            ),
//            stopSurveyCondition = null,
//            widget = RadioWidget(
//                answers = listOf(
//                    "Sì",
//                    "No"
//                )
//            )
//        )
//
//        val statuses = listOf(
//            TriageProfile(
//                id = "healthy",
//                url = "http://someuniqueurl.com/healthy",
//                severity = Severity.LOW
//            ),
//            TriageProfile(
//                id = "fever",
//                url = "http://someuniqueurl.com/fever",
//                severity = Severity.MID
//            ),
//            TriageProfile(
//                id = "covid_positive",
//                url = "http://someuniqueurl.com/covid_positive",
//                severity = Severity.HIGH
//            )
//        )
//
//        val healthy = TriageCondition(
//            statuses.first().id,
//            Condition(
//                listOf()
//            )
//        )
//        val withFever = TriageCondition(
//            statuses[1].id,
//            Condition(
//                listOf(
//                    hasFever
//                )
//            )
//        )
//        val covid19Positive = TriageCondition(
//            statuses.last().id,
//            Condition(
//                listOf(
//                    hasNausea,
//                    hasFever
//                )
//            )
//        )
//
//        val survey = Survey(
//            version = "20200319",
//            logicVersion = "20200320",
//            questions = listOf(
//                question1,
//                question2,
//                question3,
//                question4,
//                question5,
//                question6,
//                question7,
//                question8
//            ),
//            triage = Triage(
//                profiles = statuses,
//                conditions = listOf(
//                    covid19Positive,
//                    withFever,
//                    healthy
//                )
//            )
//        )
//
//        val answers = mapOf(
//            question1.id to listOf(
//                SimpleAnswer(1)
//            ),
//            question2.id to listOf(
//                SimpleAnswer(1),
//                SimpleAnswer(2)
//            ),
//            question3.id to listOf(
//                CompositeAnswer(
//                    listOf(
//                        (question3.widget as PickerWidget).components[0].indexOf("38"),
//                        (question3.widget as PickerWidget).components[1].indexOf(".5")
//                    )
//                )
//            )
//        )
//
//        assertTrue("Should show question 2", question2.shouldBeShown(null, answers))
//        assertTrue("Should show question 3", question3.shouldBeShown(null, answers))
//        assertTrue("Should show question 4", question4.shouldBeShown(null, answers))
//        assertFalse("Should not show question 5", question5.shouldBeShown(null, answers))
//        assertTrue("Should not show question 6", question6.shouldBeShown(null, answers))
//        assertTrue("Should not show question 7", question7.shouldBeShown(null, answers))
//        assertTrue("Should not show question 8", question8.shouldBeShown(null, answers))
//
//        assertEquals("Has Covid-19", statuses.last(), survey.triage(null, answers))
//    }
}
