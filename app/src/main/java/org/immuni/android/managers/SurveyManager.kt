package org.immuni.android.managers

import android.content.Context
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.pico.Pico
import org.immuni.android.models.User
import org.immuni.android.models.HealthProfile
import org.immuni.android.models.HealthProfileIdList
import org.immuni.android.models.survey.*
import org.immuni.android.ui.log.model.FormModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SurveyManager : KoinComponent {
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
    private val userManager: UserManager by inject()
    private val pico: Pico by inject()

    init {
        additionalDelay = getOrSetAdditionalDelay(storage)
    }

    fun healthProfileIds(userId: String): List<String> {
        return storage.load<HealthProfileIdList>(HealthProfileIdList.id(userId))?.profileIds
            ?: listOf()
    }

    private fun deleteHealthProfileIds(userId: String) {
        storage.delete(HealthProfileIdList.id(userId))
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

    private fun deleteHealthProfile(healthProfileId: String) {
        storage.delete(healthProfileId)
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

    private fun deleteLastAnsweredQuestions(userId: String) {
        storage.delete(answeredQuestionsKey(userId))
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

    fun isSurveyCompletedForUser(userId: String): Boolean {
        val lastHealthProfile = lastHealthProfile(userId)
        val lastSurveyDate = lastHealthProfile?.surveyDate ?: return false

        return lastSurveyDate > lastAvailableSurveyDate()
    }

    private fun todayAtMidnight(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun todaySurveyCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
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

    fun nextUserToLog(): User? {
        return userManager.users().find {
            !isSurveyCompletedForUser(it.id)
        }
    }

    fun usersToLogCount(): Int {
        return userManager.users().count {
            !isSurveyCompletedForUser(it.id)
        }
    }

    fun areAllSurveysLogged(): Boolean {
        return nextUserToLog() == null
    }

    fun deleteUserData(userId: String) {
        userManager.deleteUser(userId)

        healthProfileIds(userId).map { deleteHealthProfile(it) }
        deleteHealthProfileIds(userId)
    }

    fun deleteDataOlderThan(days: Int) {
        val thresholdDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        userManager.users().map { user ->
            allHealthProfiles(user.id).map { healthProfile ->
                if (healthProfile.surveyDate < thresholdDate) {
                    deleteHealthProfile(healthProfile.id)
                }
            }
            deleteLastAnsweredQuestions(user.id)
        }
    }
}
