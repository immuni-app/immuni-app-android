package org.immuni.android.ui.log

import androidx.lifecycle.*
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.UserManager
import org.immuni.android.models.User
import org.immuni.android.models.survey.*
import org.immuni.android.ui.log.fragment.model.QuestionType
import org.immuni.android.ui.log.fragment.model.ResumeItemType
import org.immuni.android.ui.log.fragment.model.UserType
import org.immuni.android.ui.log.model.FormModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class LogViewModel(
    private val handle: SavedStateHandle
) : ViewModel(), KoinComponent {

    private val state: KVStorage by inject()
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val concierge: ConciergeManager by inject()
    private val pico: Pico by inject()
    private val userManager: UserManager by inject()
    private val surveyManager: SurveyManager by inject()
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var user = MediatorLiveData<User>()

    val userIndex: Int?
        get() = user.value?.let { userManager.indexForUser(it.id) }

    val deviceId: String
        get() = concierge.backupPersistentId.id

    var survey = MutableLiveData<Survey>()

    var resumeModel = MediatorLiveData<List<ResumeItemType>>()

    // internal state
    var formModel = MediatorLiveData<FormModel>()
    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)
    // end internal state

    private val _navigateToQuestion = MutableLiveData<Event<String>>()
    val navigateToQuestion: LiveData<Event<String>>
        get() = _navigateToQuestion

    private val _navigateToResume = MutableLiveData<Event<Boolean>>()
    val navigateToResume: LiveData<Event<Boolean>>
        get() = _navigateToResume

    private val _navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage: LiveData<Event<Boolean>>
        get() = _navigateToNextPage

    private val _navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage: LiveData<Event<Boolean>>
        get() = _navigateToPrevPage

    private val _navigateToDonePage = MutableLiveData<Event<Boolean>>()
    val navigateToDonePage: LiveData<Event<Boolean>>
        get() = _navigateToDonePage

    private val _navigateToTriagePage = MutableLiveData<Event<TriageProfile>>()
    val navigateToTriagePage: LiveData<Event<TriageProfile>>
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

    fun onResumeRequested() {
        resumeModel.removeSource(user)
        resumeModel.addSource(user) { user ->
            val survey = survey.value!!
            val list = mutableListOf<ResumeItemType>()
            list.add(UserType(user, userManager.indexForUser(user.id)))

            formModel()?.surveyAnswers?.keys?.forEach { questionId ->
                val answers = formModel()?.surveyAnswers?.get(questionId)!!
                val q = survey.question(questionId)
                var answersString = q.humanReadableAnswers(answers)
                val isNoAnswer = answersString.isEmpty()
                if (isNoAnswer) {
                    answersString = ImmuniApplication.appContext.getString(R.string.no_choice)
                }
                list.add(QuestionType(q.title, answersString, isNoAnswer))
            }
            resumeModel.value = list
        }
    }

    private fun setup(reset: Boolean = false) {
        val settings = oracle.settings()!!
        val _survey = settings.survey!!
        survey.value = _survey

        uiScope.launch {
            val _user = surveyManager.nextUserToLog() ?: return@launch
            user.value = _user
            val lastProfile = surveyManager.lastHealthProfile(_user.id)
            val answeredQuestionsElapsedDays = surveyManager.answeredQuestionsElapsedDays(_user.id)

            val currentQuestion = _survey.firstQuestionToShow(
                    answeredQuestionsElapsedDays = answeredQuestionsElapsedDays,
                    healthState = lastProfile?.healthState ?: setOf(),
                    triageProfile = lastProfile?.triageProfileId

            )

            if (formModel.value == null || reset) {
                formModel.value = FormModel(
                    initialQuestion = currentQuestion.id,
                    initialHealthState = lastProfile?.healthState ?: setOf(),
                    triageProfile = lastProfile?.triageProfileId,
                    surveyAnswers = linkedMapOf(),
                    answeredQuestionsElapsedDays = answeredQuestionsElapsedDays
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
        form.saveHealthState(updatedHealthState)

        updateFormModel(form)

        val nextDestination = survey.next(
            questionId,
            form.healthState,
            form.triageProfile,
            form.surveyAnswers,
            form.answeredQuestionsElapsedDays
        )

        when (nextDestination) {
            is SurveyQuestionDestination -> {
                // avoid fast tapping and inconsistent viewpager states
                if(form.answeredQuestions.last() != nextDestination.question.id) {
                    form.advanceTo(nextDestination.question.id)
                    _navigateToQuestion.value = Event(nextDestination.question.id)
                }
            }
            is SurveyEndDestination -> {
                _navigateToResume.value = Event(true)
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

            // Show the DONE page for 2 seconds before proceed
            delay(2000)

            val triageProfile = updatedUserHealthProfile.triageProfileId?.let { profileId ->
                survey.triage.profile(profileId)?.let { profile ->
                    _navigateToTriagePage.value = Event(profile)
                    profile
                }
            }

            // TODO if triage profile is null let's log this event to Pico
            if(triageProfile == null) {
                navigateToNextStep()
            }
        }
    }

    fun navigateToNextStep() {
        if (surveyManager.areAllSurveysLogged()) {
            _navigateToMainPage.value = Event(true)
        } else {
            _navigateToNextLogStartPage.value = Event(true)
        }
    }

    fun getProgressPercentage(pos: Int): Float {
        val form = formModel.value!!
        val survey = survey.value!!
        val totalQuestions = survey.questionCount
        if (pos == 0) return 1f / totalQuestions
        if (totalQuestions == 0) return 0f
        val currentQuestion = form.currentQuestion
        val index = survey.indexOfQuestion(currentQuestion).coerceAtLeast(0)
        return ((index + 2).toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)
    }

    fun canGoBack() = oracle.settings()?.let { !it.disableSurveyBack } ?: true

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}
