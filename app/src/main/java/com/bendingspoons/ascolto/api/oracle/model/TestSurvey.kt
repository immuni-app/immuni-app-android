val testSurveyJson = """
{
  "version": "test",
  "questions": [
    {
      "key": "gruppo_sanguineo",
      "widget": { "type": "radio", "answers": ["A", "B", "AB", "0"] },
      "id": "gruppo_sanguineo",
      "title": "Gruppo sanguigno",
      "description": "",
      "frequency": 9999,
      "only_when": [],
      "state_updater": [],
      "jump": []
    },
    {
      "key": "hai_ricevuto_esito_esame",
      "widget": {
        "type": "radio",
        "answers": ["Si", "No", "Non l'ho richiesto"]
      },
      "only_when": [
        { "type": "states_contain", "states": ["is_waiting_for_test_response"] }
      ],
      "state_updater": [
        {
          "state": "is_waiting_for_test_response",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "hai_ricevuto_esito_esame",
              "matching_indexes": [2]
            }
          ]
        }
      ],
      "jump": [
        {
          "to": "esito_esame",
          "conditions": [
            {
              "type": "simple",
              "question_id": "hai_ricevuto_esito_esame",
              "matching_indexes": [0]
            }
          ]
        },
        {
          "to": "__end__",
          "conditions": [
            {
              "type": "simple",
              "question_id": "hai_ricevuto_esito_esame",
              "matching_indexes": [1]
            }
          ]
        }
      ],
      "id": "hai_ricevuto_esito_esame",
      "title": "Hai avuto l'esito dell'esame?",
      "description": "",
      "frequency": 1
    },
    {
      "key": "hai_fatto_esame",
      "widget": { "type": "radio", "answers": ["Si", "No"] },
      "id": "hai_fatto_esame",
      "title": "Ti è stato fatto un esame per verificare infezione da Coronavirus?",
      "description": "",
      "frequency": 3,
      "only_when": [],
      "state_updater": [],
      "jump": []
    },
    {
      "key": "esito_esame",
      "widget": {
        "type": "radio",
        "answers": ["Positivo", "Negativo", "Sono in attesa del risultato"]
      },
      "only_when": [
        {
          "type": "simple",
          "question_id": "hai_fatto_esame",
          "matching_indexes": [0]
        },
        {
          "type": "simple",
          "question_id": "hai_ricevuto_esito_esame",
          "matching_indexes": [0]
        }
      ],
      "state_updater": [
        {
          "state": "is_positive",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "esito_esame",
              "matching_indexes": [2]
            }
          ]
        },
        {
          "state": "is_waiting_for_test_response",
          "type": "add",
          "conditions": [
            {
              "type": "simple",
              "question_id": "esito_esame",
              "matching_indexes": [2]
            }
          ]
        },
        {
          "state": "is_positive",
          "type": "add",
          "conditions": [
            {
              "type": "simple",
              "question_id": "esito_esame",
              "matching_indexes": [0]
            }
          ]
        },
        {
          "state": "is_waiting_for_test_response",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "esito_esame",
              "matching_indexes": [0]
            }
          ]
        },
        {
          "state": "is_positive",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "esito_esame",
              "matching_indexes": [1]
            }
          ]
        },
        {
          "state": "is_waiting_for_test_response",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "esito_esame",
              "matching_indexes": [1]
            }
          ]
        }
      ],
      "id": "esito_esame",
      "title": "Quale é stato l'esito dell'esame?",
      "description": "",
      "frequency": 1,
      "jump": []
    },
    {
      "key": "data_esame",
      "widget": {
        "type": "radio",
        "answers": ["Oggi", "Ieri", "2 Giorni Fa", "Piu di 2 giorni fa"]
      },
      "only_when": [
        {
          "type": "simple",
          "question_id": "esito_esame",
          "matching_indexes": [0]
        }
      ],
      "id": "data_esame",
      "title": "Quando ti é stato fatto?",
      "description": "",
      "frequency": 1,
      "state_updater": [],
      "jump": []
    },
    {
      "key": "quale_esame_fatto",
      "widget": {
        "type": "radio",
        "answers": ["TAC Torace", "Rx Torace", "Tampone", "Test Anticorpi"]
      },
      "only_when": [
        {
          "type": "simple",
          "question_id": "esito_esame",
          "matching_indexes": [0]
        },
        {
          "type": "simple",
          "question_id": "esito_esame",
          "matching_indexes": [1]
        }
      ],
      "id": "quale_esame_fatto",
      "title": "Quale esame ti è stato fatto?",
      "description": "",
      "frequency": 3,
      "state_updater": [],
      "jump": []
    },
    {
      "key": "sei_quarantena_abitazione",
      "widget": { "type": "radio", "answers": ["Si", "No"] },
      "only_when": [
        {
          "type": "simple",
          "question_id": "esito_esame",
          "matching_indexes": [0]
        }
      ],
      "jump": [
        {
          "to": "malattie_a_rischio",
          "conditions": [
            {
              "type": "simple",
              "question_id": "sei_quarantena_abitazione",
              "matching_indexes": [0]
            }
          ]
        }
      ],
      "id": "sei_quarantena_abitazione",
      "title": "Sei in quarantena presso la tua abitazione?",
      "description": "",
      "frequency": 1,
      "state_updater": []
    },
    {
      "key": "sei_ricoverato",
      "widget": { "type": "radio", "answers": ["Si", "No"] },
      "only_when": [
        {
          "type": "simple",
          "question_id": "esito_esame",
          "matching_indexes": [0]
        }
      ],
      "id": "sei_ricoverato",
      "title": "Sei attualmente ricoverato?",
      "description": "",
      "frequency": 1,
      "state_updater": [],
      "jump": []
    },
    {
      "key": "malattie_a_rischio",
      "widget": {
        "type": "multiple_choices",
        "min_answers": 1,
        "max_answers": 12,
        "answers": [
          "Malattie Polmonari",
          "Malattie cardiache",
          "Malattie renali",
          "Malattie sistema immunitario",
          "Malattie oncologiche",
          "Malattie metaboliche",
          "Gravidanza",
          "Isolamento sociale",
          "Non autosufficiente",
          "Operatore sanitario",
          "Fumatore",
          "Nessuna di questi"
        ]
      },
      "id": "malattie_a_rischio",
      "title": "Hai malattie a rischio?",
      "description": "",
      "frequency": 9999,
      "only_when": [],
      "state_updater": [],
      "jump": []
    },
    {
      "key": "vaccino_antiinfluenzale",
      "widget": { "type": "radio", "answers": ["Si", "No", "Non so"] },
      "id": "vaccino_antiinfluenzale",
      "title": "Hai fatto il vaccino antinfluenzale quest'anno?",
      "description": "",
      "frequency": 9999,
      "only_when": [],
      "state_updater": [],
      "jump": []
    },
    {
      "key": "vaccino_pneumococco",
      "widget": { "type": "radio", "answers": ["Si", "No", "Non so"] },
      "id": "vaccino_pneumococco",
      "title": "Hai fatto il vaccino anti pneumococco?",
      "description": "",
      "frequency": 9999,
      "only_when": [],
      "state_updater": [],
      "jump": []
    },
    {
      "key": "quale_vaccino_pneumococco",
      "widget": {
        "type": "radio",
        "answers": ["Prevnar 13", "Pneumovax 23", "Non ricordo"]
      },
      "only_when": [
        {
          "type": "simple",
          "question_id": "vaccino_pneumococco",
          "matching_indexes": [0]
        }
      ],
      "id": "quale_vaccino_pneumococco",
      "title": "Quale vaccino anti pneumococco?",
      "description": "",
      "frequency": 9999,
      "state_updater": [],
      "jump": []
    },
    {
      "key": "contatto_con_casi_confermati",
      "widget": { "type": "radio", "answers": ["Si", "No", "Non so"] },
      "state_updater": [
        {
          "state": "stretto_contatto_confermati",
          "type": "add",
          "conditions": [
            {
              "type": "simple",
              "question_id": "contatto_con_casi_confermati",
              "matching_indexes": [0]
            }
          ]
        },
        {
          "state": "stretto_contatto_confermati",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "contatto_con_casi_confermati",
              "matching_indexes": [1]
            }
          ]
        }
      ],
      "id": "contatto_con_casi_confermati",
      "title": "Nelle ultime due settimane sei entrato a stretto contatto con casi confermati?",
      "description": "",
      "frequency": 3,
      "only_when": [],
      "jump": []
    },
    {
      "key": "contatto_con_sospetti_o_probabili",
      "widget": { "type": "radio", "answers": ["Si", "No", "Non so"] },
      "state_updater": [
        {
          "state": "stretto_contatto_sospetti",
          "type": "add",
          "conditions": [
            {
              "type": "simple",
              "question_id": "contatto_con_sospetti_o_probabili",
              "matching_indexes": [0]
            }
          ]
        },
        {
          "state": "stretto_contatto_sospetti",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "contatto_con_sospetti_o_probabili",
              "matching_indexes": [1]
            }
          ]
        }
      ],
      "id": "contatto_con_sospetti_o_probabili",
      "title": "Nelle ultime due settimane sei entrato a stretto contatto con casi sospetti o probabili?",
      "description": "",
      "frequency": 3,
      "only_when": [],
      "jump": []
    },
    {
      "key": "contatto_con_familiari_di_sospetti",
      "widget": { "type": "radio", "answers": ["Si", "No", "Non so"] },
      "state_updater": [
        {
          "state": "stretto_contatto_familiari_sospetti",
          "type": "add",
          "conditions": [
            {
              "type": "simple",
              "question_id": "contatto_con_familiari_di_sospetti",
              "matching_indexes": [0]
            }
          ]
        },
        {
          "state": "stretto_contatto_familiari_sospetti",
          "type": "remove",
          "conditions": [
            {
              "type": "simple",
              "question_id": "contatto_con_familiari_di_sospetti",
              "matching_indexes": [1]
            }
          ]
        }
      ],
      "id": "contatto_con_familiari_di_sospetti",
      "title": "Nelle ultime due settimane sei entrato in stretto contatto con familiari o conviventi di casi sospetti",
      "description": "",
      "frequency": 3,
      "only_when": [],
      "jump": []
    },
    {
      "key": "cambiato_qualcosa",
      "widget": { "type": "radio", "answers": ["Si", "No", "Non so"] },
      "jump": [
        {
          "to": "__end__",
          "conditions": [
            {
              "type": "simple",
              "question_id": "cambiato_qualcosa",
              "matching_indexes": [1, 2]
            }
          ]
        }
      ],
      "id": "cambiato_qualcosa",
      "title": "E' cambiato qualcosa dall'ultima compilazione?",
      "description": "",
      "frequency": 1,
      "only_when": [],
      "state_updater": []
    },
    {
      "key": "temperatura",
      "widget": {
        "type": "picker",
        "components": [
          ["35", "36", "37", "38", "39", "40", "41"],
          [".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"]
        ]
      },
      "state_updater": [
        {
          "state": "survey_completed_at_least_once",
          "type": "add",
          "conditions": []
        },
        {
          "state": "has_high_fever",
          "type": "add",
          "conditions": [
            {
              "type": "composite",
              "question_id": "temperatura",
              "matching_component_indexes": [
                [2, 6],
                [2, 7],
                [2, 8],
                [2, 9],
                [3, null],
                [4, null],
                [5, null],
                [6, null]
              ]
            }
          ]
        },
        {
          "state": "has_low_fever",
          "type": "remove",
          "conditions": [
            {
              "type": "composite",
              "question_id": "temperatura",
              "matching_component_indexes": [
                [2, 6],
                [2, 7],
                [2, 8],
                [2, 9],
                [3, null],
                [4, null],
                [5, null],
                [6, null]
              ]
            }
          ]
        },
        {
          "state": "has_low_fever",
          "type": "add",
          "conditions": [
            {
              "type": "composite",
              "question_id": "temperatura",
              "matching_component_indexes": [
                [2, 0],
                [2, 1],
                [2, 2],
                [2, 3],
                [2, 4],
                [2, 5]
              ]
            }
          ]
        },
        {
          "state": "has_high_fever",
          "type": "remove",
          "conditions": [
            {
              "type": "composite",
              "question_id": "temperatura",
              "matching_component_indexes": [
                [2, 0],
                [2, 1],
                [2, 2],
                [2, 3],
                [2, 4],
                [2, 5]
              ]
            }
          ]
        },
        {
          "state": "has_low_fever",
          "type": "remove",
          "conditions": [
            {
              "type": "composite",
              "question_id": "temperatura",
              "matching_component_indexes": [
                [0, null],
                [1, null]
              ]
            }
          ]
        },
        {
          "state": "has_high_fever",
          "type": "remove",
          "conditions": [
            {
              "type": "composite",
              "question_id": "temperatura",
              "matching_component_indexes": [
                [0, null],
                [1, null]
              ]
            }
          ]
        }
      ],
      "id": "temperatura",
      "title": "Quale é la tua temperatura?",
      "description": "",
      "frequency": 1,
      "only_when": [],
      "jump": []
    },
    {
      "key": "respiro_affannoso",
      "widget": { "type": "radio", "answers": ["Si", "No"] },
      "jump": [
        {
          "to": "__end__",
          "conditions": [
            {
              "type": "simple",
              "question_id": "respiro_affannoso",
              "matching_indexes": [0]
            },
            { "type": "states_contain", "states": ["has_high_fever"] }
          ]
        }
      ],
      "id": "respiro_affannoso",
      "title": "In questo momento hai il respiro affannoso",
      "description": "",
      "frequency": 1,
      "only_when": [],
      "state_updater": []
    },
    {
      "key": "sintomi_lievi",
      "widget": {
        "type": "multiple_choices",
        "min_answers": 1,
        "max_answers": 11,
        "answers": [
          "difficoltà respiratoria",
          "tosse secca",
          "naso che cola",
          "mal di gola o gola molto secca",
          "disturbi gastrointestinali",
          "mal di testa",
          "senso di spossatezza",
          "perdita del gusto e dell’olfatto",
          "perdita di equilibrio",
          "tachicardia",
          "nessun sintomo"
        ]
      },
      "id": "sintomi_lievi",
      "title": "In questo momento hai uno o più tra questi sintomi?",
      "description": "",
      "frequency": 1,
      "only_when": [],
      "state_updater": [],
      "jump": []
    },
    {
      "key": "tachicardia_frequenza_polso",
      "widget": {
        "type": "picker",
        "components": [["Non so", "35", "36", "37", "38", "39", "40", "41"]]
      },
      "only_when": [
        {
          "type": "simple",
          "question_id": "sintomi_lievi",
          "matching_indexes": [9]
        }
      ],
      "id": "tachicardia_frequenza_polso",
      "title": "Se hai un cardiofrequenzimetro (anche quelli da polso) dimmi la frequenza",
      "description": "",
      "frequency": 1,
      "state_updater": [],
      "jump": []
    }
  ],
  "triage": {
    "profiles": [
      {
        "id": "positivo",
        "severity": "high",
        "url": "https://content.ascolto-onlus.org/77e2e08cd1b72d9529493b8fabcb8804/5b35a7d7f1fa8119cde5d6702806cbb4.html#/temperature/danger"
      },
      {
        "id": "sintomi_gravi",
        "severity": "high",
        "url": "https://content.ascolto-onlus.org/77e2e08cd1b72d9529493b8fabcb8804/5b35a7d7f1fa8119cde5d6702806cbb4.html#/temperature/danger"
      },
      {
        "id": "sano_con_contatto_accertato",
        "severity": "mid",
        "url": "https://content.ascolto-onlus.org/77e2e08cd1b72d9529493b8fabcb8804/5b35a7d7f1fa8119cde5d6702806cbb4.html#/temperature/warning"
      },
      {
        "id": "sano_no_contatto",
        "severity": "low",
        "url": "https://content.ascolto-onlus.org/77e2e08cd1b72d9529493b8fabcb8804/5b35a7d7f1fa8119cde5d6702806cbb4.html#/temperature/fine"
      },
      {
        "id": "sintomi_lievi_no_contatto",
        "severity": "mid",
        "url": "https://content.ascolto-onlus.org/77e2e08cd1b72d9529493b8fabcb8804/5b35a7d7f1fa8119cde5d6702806cbb4.html#/temperature/warning"
      }
    ],
    "logic": [
      {
        "profile_id": "positivo",
        "conditions": [{ "type": "states_contain", "states": ["is_positive"] }]
      },
      {
        "profile_id": "sintomi_gravi",
        "conditions": [
          { "type": "states_contain", "states": ["has_high_fever"] }
        ]
      },
      {
        "profile_id": "sintomi_lievi_no_contatto",
        "conditions": [
          {
            "type": "states_not_contain",
            "states": [
              "stretto_contatto_confermati",
              "stretto_contatto_familiari_sospetti"
            ]
          },
          { "type": "states_contain", "states": ["has_low_fever"] }
        ]
      },
      {
        "profile_id": "sano_con_contatto_accertato",
        "conditions": [
          {
            "type": "states_contain",
            "states": [
              "stretto_contatto_confermati",
              "stretto_contatto_familiari_sospetti"
            ]
          },
          {
            "type": "states_not_contain",
            "states": ["has_high_fever", "has_low_fever"]
          }
        ]
      },
      {
        "profile_id": "sano_no_contatto",
        "conditions": [
          {
            "type": "states_not_contain",
            "states": [
              "stretto_contatto_confermati",
              "stretto_contatto_familiari_sospetti"
            ]
          },
          {
            "type": "states_not_contain",
            "states": ["has_high_fever", "has_low_fever"]
          }
        ]
      },
      { "profile_id": "sano_no_contatto", "conditions": [] }
    ]
  }
}
""".trimIndent()
