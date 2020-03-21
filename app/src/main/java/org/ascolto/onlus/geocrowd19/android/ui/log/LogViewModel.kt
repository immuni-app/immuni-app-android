package org.ascolto.onlus.geocrowd19.android.ui.log

import androidx.lifecycle.*
import com.bendingspoons.ascolto.api.oracle.model.AscoltoMe
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.ascolto.api.oracle.model.getSettingsSurvey
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.ascolto.db.entity.UserInfoEntity
import com.bendingspoons.ascolto.models.survey.*
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class LogViewModel(val handle: SavedStateHandle, private val database: AscoltoDatabase) :
    ViewModel(),
    KoinComponent {

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
            formModel.value = handle.get<Serializable>(STATE_KEY) as FormModel
        } else formModel.value = FormModel()

        formModel.addSource(savedStateLiveData) {
            formModel.value = it as? FormModel
        }
        // end internal state

        // TODO select the user for the survay (main or family member)
        uiScope.launch {
            val allUsers = database.userInfoDao().getFamilyMembersUserInfo()
            //TODO use the logic to choose the correct user
            userInfo.value = database.userInfoDao().getMainUserInfo()
        }

        // TODO load current survey from settings
        survey.value = getSettingsSurvey()?.survey()!!
    }

    fun onNextTap(questionId: String) {
        val form = formModel.value
        val survey = survey.value
        if (form == null || survey == null) {
            assert(false) { "FormModel and Survey should never be null here" }
            return
        }

        // override current if any and delete ahead
        form.addAnswers(questionId, form.surveyAnswers[questionId] ?: listOf())

        val updatedHealthState = survey.updatedHealthState(
            questionId,
            form.healthState,
            form.triageProfile,
            form.surveyAnswers
        )
        form.healthState = updatedHealthState

        handle.set(STATE_KEY, form)

        val nextDestination = survey.next(
            questionId,
            form.healthState,
            form.triageProfile,
            form.surveyAnswers
        )

        when (nextDestination) {
            is SurveyQuestionDestination -> {
                _navigateToQuestion.value = Event(nextDestination.question.id)
            }
            is SurveyEndDestination -> {
                _navigateToDonePage.value = Event(true)
            }
        }
    }

    fun onPrevTap(questionId: String) {
        _navigateToPrevPage.value = Event(true)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateFormModel(model: FormModel) {
        handle.set(STATE_KEY, model)
    }

    // For multiple choice widgets
    fun saveAnswers(questionId: String, answers: QuestionAnswers) {
        formModel.value?.addAnswers(questionId, answers)
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
            // TODO this are the survey data
            val surveyVersion = survey.value!!.version
            val userId = userInfo.value!!.id
            val answers = formModel.value!!.surveyAnswers.filterKeys { questionId ->
                questionId in formModel.value!!.answeredQuestionsOrdered
            }
            //val triage = survey.value!!.triage(null, answers)
            // TODO send to Pico
            /*
            pico.trackUserAction(
                "survey",
                    "user_id" to userId,
                    "version" to surveyVersion,
                    "answers" to answers  // TODO check correct serialization of Answer, or do custom list
                )
             */

            // TODO do we need to store the data in local database too?
            // At least the last user timestamp
            delay(2000)

            // TODO now choose another family member to do the survet, or go home if there is not
            _navigateToMainPage.value = Event(true)
        }
    }

    fun getProgressPercentage(pos: Int): Float {
        val totalQuestions = survey.value?.questions?.size ?: 0
        if (pos == 0) return 1f / totalQuestions
        if (totalQuestions == 0) return 0f
        val currentQuestion = formModel.value?.answeredQuestionsOrdered?.getOrNull(pos - 1)
        val index =
            survey.value?.questions?.map { it.id }?.indexOf(currentQuestion)?.coerceAtLeast(0) ?: 0
        return ((index + 2).toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)
    }

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}