package org.immuni.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bendingspoons.base.storage.KVStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.immuni.android.managers.SurveyManager
import org.immuni.android.models.HealthProfile
import org.immuni.android.models.survey.*
import org.immuni.android.ui.log.model.FormModel
import org.immuni.android.util.fromJson
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import java.io.IOException
import java.util.*
import kotlin.collections.LinkedHashMap

@RunWith(AndroidJUnit4::class)
class StateSerializationTests {
    //    private lateinit var store: KVStorage
    private lateinit var surveyManager: SurveyManager

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
//        store = KVStorage(name = "test", context = context, cacheInMemory = true, encrypted = true)
        surveyManager = SurveyManager()
    }

    @After
    @Throws(IOException::class)
    fun after() {
//        store.clear()
        surveyManager.deleteDataOlderThan(-1)
    }

    @Test
    fun serializeSurveyResponses() {
        surveyManager.deleteUserData("userId")

        val originalProfiles: MutableList<HealthProfile> = mutableListOf()

        runBlocking {
            for (i in 1..10) {
                val answers = linkedMapOf<QuestionId, QuestionAnswers>()
                for (j in 1..20) {
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

                delay(100)
            }
        }

        val decodedProfiles = surveyManager.allHealthProfiles("userId")

        assertEquals(originalProfiles, decodedProfiles)
    }
}
