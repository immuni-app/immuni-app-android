package com.bendingspoons.ascolto.api.oracle.model

import com.bendingspoons.ascolto.models.survey.raw.RawSurvey
import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
class AscoltoSettings(
    // app specific properties
    @field:Json(name = "development_devices") val developmentDevices: List<String> = listOf()
) : OracleSettings()

fun getSettingsSurvey(): RawSurvey? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(RawSurvey::class.java)
    val survey = adapter.fromJson(surveyJson)
    return survey
}

val surveyJson: String = """
        {
            "version": "20200319",
            "logic_version": "20200319",
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
