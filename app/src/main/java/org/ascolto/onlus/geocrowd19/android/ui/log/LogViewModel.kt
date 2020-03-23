package org.ascolto.onlus.geocrowd19.android.ui.log

import android.content.Intent
import androidx.lifecycle.*
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.managers.SurveyManager
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.models.survey.*
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
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
    private val surveyManager: SurveyManager by inject()
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var user = MediatorLiveData<User>()

    var survey = MutableLiveData<Survey>()

    // internal state
    var formModel = MediatorLiveData<FormModel>()
    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)
    // end internal state

    private val _navigateToQuestion = MutableLiveData<Event<String>>()
    val navigateToQuestion: LiveData<Event<String>>
        get() = _navigateToQuestion

    private val _navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage: LiveData<Event<Boolean>>
        get() = _navigateToNextPage

    private val _navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage: LiveData<Event<Boolean>>
        get() = _navigateToPrevPage

    private val _navigateToDonePage = MutableLiveData<Event<Boolean>>()
    val navigateToDonePage: LiveData<Event<Boolean>>
        get() = _navigateToDonePage

    private val _navigateToTriagePage = MutableLiveData<Event<Boolean>>()
    val navigateToTriagePage: LiveData<Event<Boolean>>
        get() = _navigateToTriagePage

    private val _navigateToNextLogStartPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextLogStartPage: LiveData<Event<Boolean>>
        get() = _navigateToNextLogStartPage

    private val _navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToMainPage: LiveData<Event<Boolean>>
        get() = _navigateToMainPage

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

        setup()
    }

    private fun setup(reset: Boolean = false) {
        val settings = oracle.settings()!!
        val _survey = settings.survey!!
        survey.value = _survey

        uiScope.launch {
            val _user = surveyManager.nextUserToLog()!!
            user.value = _user
            val lastProfile = surveyManager.userHealthProfile(_user.id)

            val currentQuestion = _survey.questions.first {
                it.shouldBeShown(
                    healthState = lastProfile.healthState,
                    triageProfile = lastProfile.triageProfileId,
                    surveyAnswers = linkedMapOf()
                )
            }.id

            if (formModel.value == null || reset) {
                formModel.value = FormModel(
                    questionHistory = Stack<QuestionId>().apply { push(currentQuestion) },
                    healthState = lastProfile.healthState,
                    triageProfile = lastProfile.triageProfileId,
                    surveyAnswers = linkedMapOf()
                )
                handle.set(STATE_KEY, formModel.value)
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

    fun reset() {
        setup(reset = true)
    }

    fun formModel(): FormModel? {
        return formModel.value
    }

    fun onLogComplete() {
        //val userInfo = partialUserInfo.value!!
        uiScope.launch {
            val form = formModel.value!!
            val survey = survey.value!!
            val userId = user.value!!.id

            form.triageProfile = survey.triage(
                form.healthState,
                form.triageProfile,
                form.surveyAnswers
            )?.id

            updateFormModel(form)

            val updatedUserHealthProfile =
                surveyManager.completeSurvey(userId = userId, form = form, survey = survey)

            // At least the last user timestamp
            delay(2000)

            updatedUserHealthProfile.triageProfileId?.let { profileId ->
                survey.triage.profile(profileId)?.let { profile ->
                    openTriageDialog(profile)
                }
            }

            // TODO: check why removing this delay prevents the Triage dialog webView from showing
            delay(1000)

            if (surveyManager.areAllSurveysLogged()) {
                _navigateToMainPage.value = Event(true)
            } else {
                _navigateToNextLogStartPage.value = Event(true)
            }
        }
    }

    private fun openTriageDialog(triageProfile: TriageProfile) {
        val context = AscoltoApplication.appContext
        val intent = Intent(context, WebViewDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("url", triageProfile.url)
        }
        context.startActivity(intent)
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
