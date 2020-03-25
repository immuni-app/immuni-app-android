package org.ascolto.onlus.geocrowd19.android.managers

import android.content.Context
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.models.HealthProfile
import org.ascolto.onlus.geocrowd19.android.models.HealthProfileIdList
import org.ascolto.onlus.geocrowd19.android.models.survey.*
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SurveyManager(private val context: Context) : KoinComponent {
    companion object {
        private const val additionalDelayKey = "additionalDelay"

        private fun answeredQuestionsKey(userId: String) = "answeredQuestions-$userId"

        fun getOrSetAdditionalDelay(storage: KVStorage): Int {
            val storedValue = storage.load<Int>(additionalDelayKey)
            if (storedValue != null) {
                return storedValue
            }

            // a delay between 15 minutes and 2 hours in seconds
            val newValue = Random(Date().time).nextInt(15 * 60, 2 * 60 * 60)
            storage.save(additionalDelayKey, newValue)
            return newValue
        }
    }

    private val additionalDelay: Int
    private val storage: KVStorage by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val pico: Pico by inject()

    init {
        additionalDelay = getOrSetAdditionalDelay(storage)
    }

    fun healthProfileIds(userId: String): List<String> {
        return storage.load<HealthProfileIdList>(HealthProfileIdList.id(userId))?.profileIds
            ?: listOf()
    }

    private fun saveHealthProfile(healthProfile: HealthProfile) {
        val healthProfileIdList = storage.load(
            HealthProfileIdList.id(healthProfile.userId),
            defValue = HealthProfileIdList(userId = healthProfile.userId, profileIds = listOf())
        )
        storage.save(healthProfileIdList.id, healthProfileIdList.copy(
            profileIds = healthProfileIdList.profileIds.toMutableList().apply {
                add(healthProfile.id)
            }
        ))
        storage.save(healthProfile.id, healthProfile)
    }

    fun lastHealthProfile(userId: String): HealthProfile? {
        val profileIds = healthProfileIds(userId)
        return if (profileIds.isNotEmpty()) storage.load(profileIds.last()) else null
    }

    fun allHealthProfiles(userId: String): List<HealthProfile> {
        return healthProfileIds(userId).mapNotNull { storage.load<HealthProfile>(it) }
    }

    private fun loadLastAnsweredQuestions(userId: String): Map<QuestionId, Long> {
        return storage.load(answeredQuestionsKey(userId), defValue = mapOf())
    }

    private fun saveAnseredQuestions(userId: String, questions: Set<QuestionId>) {
        val lastAnsweredQuestions = loadLastAnsweredQuestions(userId).toMutableMap()
        val date = todayAtMidnight()
        lastAnsweredQuestions.putAll(questions.map { it to date.time })
        storage.save(answeredQuestionsKey(userId), lastAnsweredQuestions)
    }

    fun answeredQuestionsElapsedDays(userId: String): Map<QuestionId, Int> {
        val lastAnsweredQuestions = loadLastAnsweredQuestions(userId)
        val date = todayAtMidnight()
        return lastAnsweredQuestions.mapValues {
            val days = TimeUnit.MILLISECONDS.toDays(date.time - it.value).toInt()
            days
        }
    }

    fun completeSurvey(userId: String, form: FormModel, survey: Survey): HealthProfile {
        val updatedHealthProfile = HealthProfile(
            userId = userId,
            healthState = form.healthState,
            triageProfileId = form.triageProfile,
            surveyVersion = survey.version,
            surveyDate = form.startDate,
            surveyAnswers = form.surveyAnswers.mapValues {
                it.value.map { answer ->
                    when (answer) {
                        is SimpleAnswer -> answer.index
                        is CompositeAnswer -> answer.componentIndexes
                    }
                }
            }
        )
        saveHealthProfile(updatedHealthProfile)
        saveAnseredQuestions(userId, form.answeredQuestions.toSet())

        return updatedHealthProfile
    }

    fun isSurveyCompletedForUser(user: User): Boolean {
        val lastHealthProfile = lastHealthProfile(user.id)
        val lastSurveyDate = lastHealthProfile?.surveyDate ?: return false

        return lastSurveyDate > lastAvailableSurveyDate()
    }

    private fun todayAtMidnight(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun todaySurveyCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.SECOND, additionalDelay)
        }
    }

    fun nextSurveyDate(): Date {
        val gc = todaySurveyCalendar()
        val todaySurveyDate = gc.time
        if (todaySurveyDate > Date()) {
            return todaySurveyDate
        }
        gc.add(Calendar.DATE, 1)
        return gc.time
    }

    fun lastAvailableSurveyDate(): Date {
        val gc = todaySurveyCalendar()
        val todaySurveyDate = gc.time
        if (todaySurveyDate < Date()) {
            return todaySurveyDate
        }
        gc.add(Calendar.DATE, -1)
        return gc.time
    }

    fun allUsers(): List<User> {
        val me: AscoltoMe = oracle.me() ?: return listOf()
        val mainUser = me.mainUser ?: return listOf()
        return listOf(mainUser) + me.familyMembers
    }

    fun nextUserToLog(): User? {
        return allUsers().find {
            !isSurveyCompletedForUser(it)
        }
    }

    fun usersToLogCount(): Int {
        return allUsers().count {
            !isSurveyCompletedForUser(it)
        }
    }

    fun areAllSurveysLogged(): Boolean {
        return nextUserToLog() == null
    }
}
