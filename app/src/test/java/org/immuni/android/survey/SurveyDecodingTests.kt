package org.immuni.android.survey

import org.immuni.android.base.utils.fromJson
import com.squareup.moshi.Moshi
import org.immuni.android.models.survey.*
import org.immuni.android.models.survey.raw.*
import org.junit.Test

import org.junit.Assert.*
import testSurveyJson

class SurveyDecodingTests {
    @Test
    fun `test survey decodes from json`() {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RawSurvey::class.java)
        val survey = adapter.fromJson(testSurveyJson)
        assertNotNull("Survey decoded from json", survey)
    }

    @Test
    fun testPickerWidgetIsReadable() {
        val json = """
    {
      "type": "picker",
      "components": [
        ["35", "36", "37", "38", "39", "40", "41", "42", "43", "44"],
        [".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"]
      ]
    }
    """

        try {
            val widget = fromJson(RawQuestionWidget::class, json)!!.widget()

            val expected = PickerWidget(
                components = listOf(
                    listOf("35", "36", "37", "38", "39", "40", "41", "42", "43", "44"),
                    listOf(".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9")
                )
            )

            assertEquals(expected, widget)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testRadioWidgetIsReadable() {
        val json = """
    {
      "type": "radio",
      "answers": [
        "Si",
        "No"
      ]
    }
    """

        try {
            val widget = fromJson(RawQuestionWidget::class, json)!!.widget()

            val expected = RadioWidget(answers = listOf("Si", "No"))

            assertEquals(expected, widget)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testMultipleChoiceWidgetIsReadable() {
        val json = """
    {
      "type": "multiple_choices",
      "min_answers": 1,
      "max_answers": 5,
      "answers": [
        "Febbre",
        "Congestione Nasale",
        "Naso che cola",
        "Mal di gola"
      ]
    }
    """

        try {
            val widget = fromJson(RawQuestionWidget::class, json)!!.widget()

            val expected = MultipleChoicesWidget(
                answers = listOf("Febbre", "Congestione Nasale", "Naso che cola", "Mal di gola"),
                minNumberOfAnswers = 1,
                maxNumberOfAnswers = 5
            )

            assertEquals(expected, widget)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testTriageConditionsAreReadable() {
        val json = """
      {
        "profile_id": "covid_positive",
        "condition": {
          "type": "and",
          "conditions": [
            {
              "question_id": "temperatura",
              "type": "composite",
              "matching_component_indexes": [
                [3, 5], [3, 6], [3, 7],
                [4, null],
                [5, null]
              ]
            },
            {
              "question_id": "sintomi",
              "type": "simple",
              "matching_indexes": [
                1,
                2,
                3
              ]
            }
          ]
        }
      }
    """

        try {
            val triageCondition = fromJson(RawTriageCondition::class, json)!!.triageCondition()

            val expected = TriageCondition(
                profileId = "covid_positive",
                condition = AndCondition(
                    conditions = listOf(
                        CompositeCondition(
                            questionId = "temperatura",
                            matchingComponentIndexes = listOf(
                                listOf(3, 5),
                                listOf(3, 6),
                                listOf(3, 7),
                                listOf(4, null),
                                listOf(5, null)
                            )
                        ),
                        SimpleCondition(questionId = "sintomi", matchingIndexes = listOf(1, 2, 3))
                    )
                )
            )

            assertEquals(expected, triageCondition)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testSimpleConditionIsReadable() {
        val json = """
    {
      "question_id": "test",
      "type": "simple",
      "matching_indexes": [
        1,
        2,
        3
      ]
    }
    """

        try {
            val values = fromJson(RawCondition::class, json)!!.condition()

            val expected: Condition = SimpleCondition(
                questionId = "test",
                matchingIndexes = listOf(1, 2, 3)
            )

            assertEquals(expected, values)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testCompositeConditionIsReadable() {
        val json = """
    {
      "question_id": "test",
      "type": "composite",
      "matching_component_indexes": [
        [3, 5], [3, 6], [3, 7],
        [4, null],
        [5, null]
      ]
    }
    """

        try {
            val values = fromJson(RawCondition::class, json)!!.condition()

            val expected: Condition = CompositeCondition(
                questionId = "test",
                matchingComponentIndexes = listOf(
                    listOf(3, 5),
                    listOf(3, 6),
                    listOf(3, 7),
                    listOf(4, null),
                    listOf(5, null)
                )
            )

            assertEquals(expected, values)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testCurrentUserStatusConditionIsReadable() {
        val json = """
    {
      "type": "current_user_triage_profile",
      "matching_profiles": ["covid_positive", null]
    }
    """

        try {
            val condition = fromJson(RawCondition::class, json)!!.condition()

            val expected: Condition = TriageProfileCondition(
                matchingProfiles = listOf("covid_positive", null)
            )

            assertEquals(expected, condition)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testQuestionIsReadableWhenNullOptionals() {
        val json = """
    {
      "id": "temperatura",
      "title": "Temperatura",
      "description": "Inserisci la tua temperatura corporea",
      "frequency": 1,
      "only_when": { "type": "true" },
      "state_updater": [],
      "jump": [],
      "widget": {
        "type": "picker",
        "components": [
          ["35", "36", "37", "38", "39", "40", "41", "42", "43", "44"],
          [".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"]
        ]
      }
    }
    """

        try {
            val question = fromJson(RawQuestion::class, json)!!.question()

            val expected = Question(
                id = "temperatura",
                title = "Temperatura",
                description = "Inserisci la tua temperatura corporea",
                periodicity = 1,
                showCondition = TrueCondition,
                healthStateUpdater = HealthStateUpdater(listOf()),
                jump = Jump(listOf()),
                widget = PickerWidget(
                    components = listOf(
                        listOf("35", "36", "37", "38", "39", "40", "41", "42", "43", "44"),
                        listOf(".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9")
                    )
                )
            )

            assertEquals(expected, question)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun testQuestionIsReadableWhenValorizedOptionals() {
        val json = """
     {
       "id": "temperatura",
       "title": "Temperatura",
       "description": "Inserisci la tua temperatura corporea",
       "frequency": 1,
       "only_when": {
         "question_id": "corona_check",
         "type": "simple",
         "matching_indexes": [1, 2, 3]
       },
       "state_updater": [
         {
           "state": "State",
           "type": "add",
           "condition": { "type": "true" }
         }
       ],
       "jump": [
         {
           "to": "__end__",
           "condition": {
             "question_id": "temperatura",
             "type": "composite",
             "matching_component_indexes": [[1, 1], [1, 2]]
           }
         }
       ],
       "widget": {
        "type": "picker",
        "components": [
          ["35", "36", "37", "38", "39", "40", "41", "42", "43", "44"],
          [".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"]
        ]
      }
    }
    """

        try {
            val question = fromJson(RawQuestion::class, json)!!.question()

            val expected = Question(
                id = "temperatura",
                title = "Temperatura",
                description = "Inserisci la tua temperatura corporea",
                periodicity = 1,
                showCondition = SimpleCondition(
                    questionId = "corona_check",
                    matchingIndexes = listOf(1, 2, 3)
                ),
                healthStateUpdater = HealthStateUpdater(
                    listOf(
                        HealthStateUpdaterItem(
                            state = "State",
                            type = HealthStateUpdaterItemType.ADD,
                            condition = TrueCondition
                        )
                    )
                ),
                jump = Jump(
                    listOf(
                        JumpItem(
                            destination = EndOfSurveyJumpDestination,
                            condition = CompositeCondition(
                                questionId = "temperatura",
                                matchingComponentIndexes = listOf(listOf(1, 1), listOf(1, 2))
                            )
                        )
                    )
                ),
                widget = PickerWidget(
                    components = listOf(
                        listOf("35", "36", "37", "38", "39", "40", "41", "42", "43", "44"),
                        listOf(".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9")
                    )
                )
            )

            assertEquals(expected, question)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }

    @Test
    fun conditionalJumperIsReadableWhenQuestionId() {
        val json = """
     {
       "to": "esito_esame",
       "condition": {
         "type": "simple",
         "question_id": "hai_ricevuto_esito_esame",
         "matching_indexes": [0]
       }
     }
    """

        try {
            val jumpItem = fromJson(
                RawJumpItem::class,
                json
            )!!.jumpItem()

            val expected = JumpItem(
                destination = QuestionJumpDestination(questionId = "esito_esame"),
                condition = SimpleCondition(
                    questionId = "hai_ricevuto_esito_esame",
                    matchingIndexes = listOf(0)
                )
            )

            assertEquals(expected, jumpItem)
        } catch (error: Exception) {
            fail("ERROR $error")
        }
    }
}
