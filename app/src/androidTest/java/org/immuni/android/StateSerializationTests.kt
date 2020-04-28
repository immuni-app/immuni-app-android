package org.immuni.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyManager
import org.immuni.android.models.HealthProfile
import org.immuni.android.models.survey.CompositeAnswer
import org.immuni.android.models.survey.QuestionAnswers
import org.immuni.android.models.survey.QuestionId
import org.immuni.android.models.survey.SimpleAnswer
import org.immuni.android.ui.log.model.FormModel
import org.immuni.android.util.log
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class StateSerializationTests {
    private lateinit var surveyManager: SurveyManager
    private lateinit var db: ImmuniDatabase

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ImmuniDatabase::class.java
        ).build()
        surveyManager = SurveyManager(db)
    }

    @After
    @Throws(IOException::class)
    fun after() {
        runBlocking {
            surveyManager.deleteDataOlderThan(-1)
        }
    }

    @Test
    fun serializeSurveyResponses() {
        runBlocking {
            surveyManager.deleteUserData("userId")

            val originalProfiles: MutableList<HealthProfile> = mutableListOf()

            for (i in 1..100) {
                val answers = linkedMapOf<QuestionId, QuestionAnswers>()
                for (j in 1..100) {
                    answers[j.toString()] = when (j % 3) {
                        0 -> listOf(SimpleAnswer(index = 0))
                        1 -> listOf(CompositeAnswer(componentIndexes = listOf(1, 2, 3)))
                        else -> listOf(
                            SimpleAnswer(index = 0),
                            SimpleAnswer(index = 1)
                        )
                    }
                }

                val form = FormModel(
                    initialQuestion = "questionId",
                    initialHealthState = setOf(),
                    triageProfile = "triageProfile",
                    surveyAnswers = answers,
                    startDate = Date(),
                    answeredQuestionsElapsedDays = mapOf()
                )
                val newProfile = surveyManager.completeSurvey("userId", form, "$i")
                originalProfiles.add(newProfile)
            }

            val decodedProfiles = surveyManager.allHealthProfiles("userId")

            assertEquals(originalProfiles, decodedProfiles)
        }
    }
}
