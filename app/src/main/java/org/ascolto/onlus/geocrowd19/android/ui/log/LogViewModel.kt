package org.ascolto.onlus.geocrowd19.android.ui.log

import androidx.lifecycle.*
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.getSettingsSurvey
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.db.entity.UserInfoEntity
import org.ascolto.onlus.geocrowd19.android.models.survey.*
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.ascolto.onlus.geocrowd19.android.models.UserHealthProfile
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable
import java.util.*

class LogViewModel(
    private val handle: SavedStateHandle,
    private val database: AscoltoDatabase
) : ViewModel(), KoinComponent {

    private val state: KVStorage by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val pico: Pico by inject()
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var userInfo = MediatorLiveData<UserInfoEntity>()

    var survey = MutableLiveData<Survey>()

    // internal state
    var formModel = MediatorLiveData<FormModel>()
    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)
    // end internal state

    private val _navigateToDonePage = MutableLiveData<Event<Boolean>>()
    val navigateToDonePage: LiveData<Event<Boolean>>
        get() = _navigateToDonePage

    private val _navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToMainPage: LiveData<Event<Boolean>>
        get() = _navigateToMainPage

    private val _navigateToQuestion = MutableLiveData<Event<String>>()
    val navigateToQuestion: LiveData<Event<String>>
        get() = _navigateToQuestion

    private val _navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage: LiveData<Event<Boolean>>
        get() = _navigateToNextPage

    private val _navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage: LiveData<Event<Boolean>>
        get() = _navigateToPrevPage

    init {
        // internal state
        if (handle.get<Serializable>(STATE_KEY) != null) {
            val form = handle.get<Serializable>(STATE_KEY) as FormModel
            formModel.value = form
        }

        formModel.addSource(savedStateLiveData) {
            formModel.value = it as? FormModel
        }
        // end internal state

        // TODO load current survey from settings
        val _survey = getSettingsSurvey()!!.survey()
        survey.value = _survey

        // TODO select the user for the survey (main or family member)
        uiScope.launch {
            val allUsers = database.userInfoDao().getFamilyMembersUserInfo()
            //TODO use the logic to choose the correct user
            userInfo.value = database.userInfoDao().getMainUserInfo()
            userInfo.value?.let { user ->
                val lastProfile = state.load(
                    UserHealthProfile.key(user.id),
                    defValue = UserHealthProfile(
                        userId = user.id,
                        healthState = setOf(),
                        triageProfileId = null,
                        lastSurveyVersion = null,
                        lastSurveyDate = null
                    )
                )

                val currentQuestion = _survey.questions.first {
                    it.shouldBeShown(
                        healthState = lastProfile.healthState,
                        triageProfile = lastProfile.triageProfileId,
                        surveyAnswers = linkedMapOf()
                    )
                }.id

                if (formModel.value == null) {
                    formModel.value = FormModel(
                        questionHistory = Stack<QuestionId>().apply { push(currentQuestion) },
                        healthState = lastProfile.healthState,
                        triageProfile = lastProfile.triageProfileId,
                        surveyAnswers = linkedMapOf()
                    )
                }
            }
        }
    }

    fun onNextTap(questionId: String) {
        val form = formModel.value
        val survey = survey.value
        if (form == null || survey == null) {
            assert(false) { "FormModel and Survey should never be null here" }
            return
        }

        val updatedHealthState = survey.updatedHealthState(
            questionId,
            form.healthState,
            form.triageProfile,
            form.surveyAnswers
        )
        form.healthState = updatedHealthState

        updateFormModel(form)

        val nextDestination = survey.next(
            questionId,
            form.healthState,
            form.triageProfile,
            form.surveyAnswers
        )

        when (nextDestination) {
            is SurveyQuestionDestination -> {
                form.advanceTo(nextDestination.question.id)
                _navigateToQuestion.value = Event(nextDestination.question.id)
            }
            is SurveyEndDestination -> {
                _navigateToDonePage.value = Event(true)
            }
        }
    }

    fun onPrevTap(questionId: String) {
        formModel.value?.goBack()
        _navigateToPrevPage.value = Event(true)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateFormModel(model: FormModel) {
        handle.set(STATE_KEY, model)
    }

    fun saveAnswers(questionId: String, answers: QuestionAnswers) {
        formModel.value?.saveAnswers(answers)
        handle.set(STATE_KEY, formModel.value)
    }

    fun resetAnswers() {
        formModel.value?.surveyAnswers?.clear()
        handle.set(STATE_KEY, formModel.value)
    }

    fun formModel(): FormModel? {
        return formModel.value
    }

    fun onLogComplete() {
        //val userInfo = partialUserInfo.value!!
        uiScope.launch {
            val form = formModel.value!!
            val survey = survey.value!!
            val userId = userInfo.value!!.id

            form.triageProfile = survey.triage(
                form.healthState,
                form.triageProfile,
                form.surveyAnswers
            )?.id

            updateFormModel(form)

            val userHealthProfile = UserHealthProfile(
                userId = userId,
                healthState = form.healthState,
                triageProfileId = form.triageProfile,
                lastSurveyVersion = survey.version,
                lastSurveyDate = Date()
            )
            state.save(userHealthProfile.key, userHealthProfile)

            // At least the last user timestamp
            delay(2000)

            // TODO send to Pico
            /*
            pico.trackUserAction(
                "survey",
                    "user_id" to userId,
                    "version" to surveyVersion,
                    "answers" to answers  // TODO check correct serialization of Answer, or do custom list
                )
             */

            // TODO now choose another family member to do the survey, or go home if there is none
            _navigateToMainPage.value = Event(true)
        }
    }

    fun getProgressPercentage(pos: Int): Float {
        val form = formModel.value!!
        val survey = survey.value!!
        val totalQuestions = survey.questions.size
        if (pos == 0) return 1f / totalQuestions
        if (totalQuestions == 0) return 0f
        val currentQuestion = form.currentQuestion
        val index =
            survey.questions.map { it.id }.indexOf(currentQuestion).coerceAtLeast(0)
        return ((index + 2).toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)
    }

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}
