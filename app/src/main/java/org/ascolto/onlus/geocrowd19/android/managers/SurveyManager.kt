package org.ascolto.onlus.geocrowd19.android.managers

import android.content.Context
import android.util.Log
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.models.UserHealthProfile
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import org.ascolto.onlus.geocrowd19.android.picoEvents.SurveyCompleted
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.random.Random

class SurveyManager(private val context: Context) : KoinComponent {
    companion object {
        private const val additionalDelayKey = "additionalDelay"
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

    fun userHealthProfile(userId: String): UserHealthProfile {
        return storage.load(UserHealthProfile.key(userId), defValue = UserHealthProfile(
            userId = userId,
            healthState = setOf(),
            triageProfileId = null,
            lastSurveyVersion = null,
            lastSurveyDate = null
        ))
    }

    fun completeSurvey(userId: String, form: FormModel, survey: Survey) : UserHealthProfile {
        val previousUserHealthProfile = userHealthProfile(userId)
        val updatedUserHealthProfile = UserHealthProfile(
            userId = userId,
            healthState = form.healthState,
            triageProfileId = form.triageProfile,
            lastSurveyVersion = survey.version,
            lastSurveyDate = form.startDate
        )
        storage.save(updatedUserHealthProfile.key, updatedUserHealthProfile)

        val surveyCompletedEvent = SurveyCompleted(
            userId = updatedUserHealthProfile.userId,
            surveyVersion = survey.version,
            answers = form.surveyAnswers,
            triageProfile = updatedUserHealthProfile.triageProfileId,
            previousUserHealthState = previousUserHealthProfile.healthState,
            userHealthState = updatedUserHealthProfile.healthState
        )
        Log.d("survey completed", surveyCompletedEvent.userAction.info.toString())

        GlobalScope.launch {
            pico.trackEvent(surveyCompletedEvent.userAction)
        }

        return updatedUserHealthProfile
    }

    fun isSurveyCompletedForUser(user: User) : Boolean {
        val userHealthProfile = userHealthProfile(user.id) ?: return false
        val lastSurveyDate = userHealthProfile.lastSurveyDate ?: return false

        return lastSurveyDate > lastAvailableSurveyDate()
    }

    fun nextSurveyDate() : Date {
        val gc = GregorianCalendar().apply {
            set(Calendar.HOUR, 6)
            set(Calendar.AM_PM, Calendar.PM)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.SECOND, additionalDelay)
        }
        val todaySurveyDate = gc.time
        if (todaySurveyDate > Date()) {
            return todaySurveyDate
        }
        gc.add(Calendar.DATE, 1)
        return gc.time
    }

    fun lastAvailableSurveyDate() : Date {
        val gc = GregorianCalendar()
        gc.set(Calendar.HOUR, 18)
        gc.set(Calendar.MINUTE, 0)
        gc.set(Calendar.SECOND, 0)
        gc.add(Calendar.SECOND, additionalDelay)
        val todaySurveyDate = gc.time
        if (todaySurveyDate < Date()) {
            return todaySurveyDate
        }

        gc.add(Calendar.DATE, -1)
        return gc.time
    }

    fun allUsers() : List<User> {
        val me: AscoltoMe = oracle.me() ?: return listOf()
        val mainUser = me.mainUser ?: return listOf()
        return listOf(mainUser) + me.familyMembers
    }

    fun nextUserToLog() : User? {
        return allUsers().find {
            !isSurveyCompletedForUser(it)
        }
    }

    fun usersToLogCount() : Int {
        return allUsers().count {
            !isSurveyCompletedForUser(it)
        }
    }

    fun areAllSurveysLogged() : Boolean {
        return nextUserToLog() == null
    }
}
