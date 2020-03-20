package com.bendingspoons.ascolto

import com.bendingspoons.ascolto.models.survey.*
import com.bendingspoons.ascolto.models.survey.raw.RawSurvey
import com.squareup.moshi.Moshi
import org.junit.Test

import org.junit.Assert.*

class SurveyUnitTests {

    val surveyJson: String = """
        {
            "version": "20200319",
            "questions": [
                {
                    "id": "question1",
                    "title": "Come stai?",
                    "description": "Dimmi se stai bene o male",
                    "frequency": 0,
                    "widget": {
                        "type": "radio",
                        "answers": [
                            "Bene",
                            "Male"
                        ]
                    }
                },
                {
                    "id": "question2",
                    "title": "Che sintomi hai?",
                    "description": "Dimmi che sintomi hai",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "simple",
                            "question_id": "question1",
                            "matching_indexes": [
                                1
                            ]
                        }
                    ],
                    "widget": {
                        "type": "multiple_choices",
                        "answers": [
                            "Mal di testa",
                            "Febbre",
                            "Nausea"
                        ]
                    }
                },
                {
                    "id": "question3",
                    "title": "Quanta febbre hai?",
                    "description": "Seleziona la temperatura",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "simple",
                            "question_id": "question2",
                            "matching_indexes": [
                                1
                            ]
                        }
                    ],
                    "widget": {
                        "type": "picker",
                        "components": [
                            [
                                "35",
                                "36",
                                "37",
                                "38",
                                "39",
                                "40",
                                "41",
                                "42"
                            ],
                            [
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
                            ]
                        ]
                    }
                },
                {
                    "id": "question4",
                    "title": "Hai mai avuto febbre così alta?",
                    "description": "Sì o no",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "composite",
                            "question_id": "question3",
                            "matching_component_indexes": [
                                [
                                    3,
                                    null
                                ]
                            ]
                        }
                    ],
                    "widget": {
                        "type": "radio",
                        "answers": [
                            "Sì",
                            "No"
                        ]
                    }
                },
                {
                    "id": "question5",
                    "title": "Hai mai avuto febbre così alta?",
                    "description": "Sì o no",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "composite",
                            "question_id": "question3",
                            "matching_component_indexes": [
                                [
                                    2,
                                    null
                                ]
                            ]
                        }
                    ],
                    "widget": {
                        "type": "radio",
                        "answers": [
                            "Sì",
                            "No"
                        ]
                    }
                },
                {
                    "id": "question6",
                    "title": "Hai mai avuto febbre così alta?",
                    "description": "Sì o no",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "composite",
                            "question_id": "question3",
                            "matching_component_indexes": [
                                [
                                    2,
                                    null
                                ],
                                [
                                    3,
                                    null
                                ]
                            ]
                        }
                    ],
                    "widget": {
                        "type": "radio",
                        "answers": [
                            "Sì",
                            "No"
                        ]
                    }
                },
                {
                    "id": "question7",
                    "title": "Hai mai avuto esattamente 38.5 di febbre?",
                    "description": "Sì o no",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "composite",
                            "question_id": "question3",
                            "matching_component_indexes": [
                                [
                                    3,
                                    5
                                ]
                            ]
                        }
                    ],
                    "widget": {
                        "type": "radio",
                        "answers": [
                            "Sì",
                            "No"
                        ]
                    }
                },
                {
                    "id": "question8",
                    "title": "Hai mai avuto sia febbre che nausea?",
                    "description": "Sì o no",
                    "frequency": 0,
                    "only_when": [
                        {
                            "type": "composite",
                            "question_id": "question3",
                            "matching_component_indexes": [
                                [
                                    2,
                                    null
                                ],
                                [
                                    3,
                                    null
                                ]
                            ]
                        },
                        {
                            "type": "simple",
                            "question_id": "question2",
                            "matching_indexes": [
                                2
                            ]
                        }
                    ],
                    "widget": {
                        "type": "radio",
                        "answers": [
                            "Sì",
                            "No"
                        ]
                    }
                }
            ],
            "triage": {
                "statuses": [
                    {
                        "id": "covid_positive",
                        "url": "http://someuniqueurl.com/covid_positive",
                        "severity": "high"
                    },
                    {
                        "id": "fever",
                        "url": "http://someuniqueurl.com/fever",
                        "severity": "mid"
                    },
                    {
                        "id": "nothing",
                        "url": "http://someuniqueurl.com/nothing",
                        "severity": "low"
                    }
                ],
                "logic": [
                    {
                        "status": "covid_positive",
                        "conditions": [
                            {
                                "type": "composite",
                                "question_id": "question3",
                                "matching_component_indexes": [
                                    [
                                        2,
                                        null
                                    ],
                                    [
                                        3,
                                        null
                                    ]
                                ]
                            }
                        ]
                    }
                ]
            }
        }
    """.trimIndent()

    @Test
    fun `survey decodes from json`() {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RawSurvey::class.java)
        val survey = adapter.fromJson(surveyJson)
        assertNotNull("Survey decoded from json", survey)
    }

    @Test
    fun `survey conditions work`() {
        val question1 = Question(
            id = "question1",
            title = "Come stai?",
            description = "Dimmi se stai bene o male",
            frequency = 0,
            showCondition = null,
            stopSurveyCondition = null,
            widget = RadioWidget(
                answers = listOf(
                    "Bene",
                    "Male"
                )
            )
        )

        val isntFeelingWell = SimpleConditionItem(
            questionId = question1.id,
            matchingIndexes = listOf(
                (question1.widget as RadioWidget).answers.indexOf("Male")
            )
        )

        val question2 = Question(
            id = "question2",
            title = "Che sintomi hai?",
            description = "Dimmi che sintomi hai",
            frequency = 0,
            showCondition = Condition(
                listOf(
                    isntFeelingWell
                )
            ),
            stopSurveyCondition = null,
            widget = MultipleChoicesWidget(
                minNumberOfAnswers = 0,
                maxNumberOfAnswers = 9999,
                answers = listOf(
                    "Mal di testa",
                    "Febbre",
                    "Nausea"
                )
            )
        )

        val hasNausea = SimpleConditionItem(
            questionId = question2.id,
            matchingIndexes = listOf(
                (question2.widget as MultipleChoicesWidget).answers.indexOf("Nausea")
            )
        )
        val hasFever = SimpleConditionItem(
            questionId = question2.id,
            matchingIndexes = listOf(
                (question2.widget as MultipleChoicesWidget).answers.indexOf("Febbre")
            )
        )
        val hasHeadache = SimpleConditionItem(
            questionId = question2.id,
            matchingIndexes = listOf(
                (question2.widget as MultipleChoicesWidget).answers.indexOf("Mal di testa")
            )
        )

        val question3 = Question(
            id = "question3",
            title = "Quanta febbre hai?",
            description = "Seleziona la temperatura",
            frequency = 0,
            showCondition = Condition(
                listOf(
                    hasFever
                )
            ),
            stopSurveyCondition = null,
            widget = PickerWidget(
                components = listOf(
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

        val has37OfFever = CompositeConditionItem(
            questionId = question3.id,
            matchingComponentIndexes = listOf(
                listOf(
                    (question3.widget as PickerWidget).components.first().indexOf("37"),
                    null
                )
            )
        )
        val has38OfFever = CompositeConditionItem(
            questionId = question3.id,
            matchingComponentIndexes = listOf(
                listOf(
                    (question3.widget as PickerWidget).components.first().indexOf("38"),
                    null
                )
            )
        )
        val has37or38OfFever = CompositeConditionItem(
            questionId = question3.id,
            matchingComponentIndexes = listOf(
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
        val hasExactly38Point5OfFever = CompositeConditionItem(
            questionId = question3.id,
            matchingComponentIndexes = listOf(
                listOf(
                    (question3.widget as PickerWidget).components[0].indexOf("38"),
                    (question3.widget as PickerWidget).components[1].indexOf(".5")
                )
            )
        )


        val question4 = Question(
            id = "question4",
            title = "Hai mai avuto febbre così alta?",
            description = "Sì o no",
            frequency = 0,
            showCondition = Condition(
                listOf(
                    has38OfFever
                )
            ),
            stopSurveyCondition = null,
            widget = RadioWidget(
                answers = listOf(
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
            showCondition = Condition(
                listOf(
                    has37OfFever
                )
            ),
            stopSurveyCondition = null,
            widget = RadioWidget(
                answers = listOf(
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
            showCondition = Condition(
                listOf(
                    has37or38OfFever
                )
            ),
            stopSurveyCondition = null,
            widget = RadioWidget(
                answers = listOf(
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
            showCondition = Condition(
                listOf(
                    hasExactly38Point5OfFever
                )
            ),
            stopSurveyCondition = null,
            widget = RadioWidget(
                answers = listOf(
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
            showCondition = Condition(
                listOf(
                    has37or38OfFever,
                    hasNausea
                )
            ),
            stopSurveyCondition = null,
            widget = RadioWidget(
                answers = listOf(
                    "Sì",
                    "No"
                )
            )
        )

        val statuses = listOf(
            TriageStatus(
                id = "healthy",
                url = "http://someuniqueurl.com/healthy",
                severity = Severity.LOW
            ),
            TriageStatus(
                id = "fever",
                url = "http://someuniqueurl.com/fever",
                severity = Severity.MID
            ),
            TriageStatus(
                id = "covid_positive",
                url = "http://someuniqueurl.com/covid_positive",
                severity = Severity.HIGH
            )
        )

        val healthy = TriageCondition(
            statuses.first().id,
            Condition(
                listOf()
            )
        )
        val withFever = TriageCondition(
            statuses[1].id,
            Condition(
                listOf(
                    hasFever
                )
            )
        )
        val covid19Positive = TriageCondition(
            statuses.last().id,
            Condition(
                listOf(
                    hasNausea,
                    hasFever
                )
            )
        )

        val survey = Survey(
            version = "20200319",
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
                statuses = statuses,
                conditions = listOf(
                    covid19Positive,
                    withFever,
                    healthy
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

        assertTrue("Should show question 2", question2.shouldBeShown(null, answers))
        assertTrue("Should show question 3", question3.shouldBeShown(null, answers))
        assertTrue("Should show question 4", question4.shouldBeShown(null, answers))
        assertFalse("Should not show question 5", question5.shouldBeShown(null, answers))
        assertTrue("Should not show question 6", question6.shouldBeShown(null, answers))
        assertTrue("Should not show question 7", question7.shouldBeShown(null, answers))
        assertTrue("Should not show question 8", question8.shouldBeShown(null, answers))

        assertEquals("Has Covid-19", statuses.last(), survey.triage(null, answers))
    }
}
