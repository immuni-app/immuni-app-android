package com.bendingspoons.ascolto.ui.log

import androidx.lifecycle.*
import com.bendingspoons.ascolto.api.oracle.model.AscoltoMe
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.ascolto.api.oracle.model.getSettingsSurvey
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.ascolto.models.survey.Answer
import com.bendingspoons.ascolto.models.survey.Survey
import com.bendingspoons.ascolto.models.survey.nextQuestion
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class LogViewModel(val handle: SavedStateHandle, private val database: AscoltoDatabase) : ViewModel(),
    KoinComponent {

    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val mainUserInfo = database.userInfoDao().getMainUserInfoLiveData()

    var survey = MutableLiveData<Survey>()

    var formModel = MediatorLiveData<FormModel>()
    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)

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
        // init
        if(handle.get<Serializable>(STATE_KEY) != null) {
            formModel.value = handle.get<Serializable>(STATE_KEY) as FormModel
        } else formModel.value = FormModel()

        formModel.addSource(savedStateLiveData) {
            formModel.value = it as? FormModel
        }

        survey.value = getSettingsSurvey()?.survey()!!
    }

    fun onNextTap(questionId: String?) {
        if(questionId != null) {
            // override current if any and delete ahead
            formModel.value?.addQuestion(questionId)
            handle.set(STATE_KEY, formModel.value)

            // 1 check the next question to be shown
            val answers = mutableMapOf<String, List<Answer>>()
            formModel.value?.answers?.keys?.forEach { i ->
                answers.put(i, listOf(formModel.value?.answers?.get(i)!!))
            }
            val nextQuestion = survey.value?.nextQuestion(questionId, answers)
            // 2 check if should stop survey or finish
            if (nextQuestion == null) {
                _navigateToDonePage.value = Event(true)
            } else {
                _navigateToQuestion.value = Event(nextQuestion.id)
            }
        } else {
            _navigateToNextPage.value = Event(true)
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

    fun saveAnswer(questionId: String, answer: Answer) {
        formModel.value?.addAnswer(questionId, answer)
        handle.set(STATE_KEY, formModel.value)
    }

    fun resetAnswers() {
        formModel.value?.answers?.clear()
        handle.set(STATE_KEY, formModel.value)
    }

    fun formModel(): FormModel? {
        return formModel.value
    }

    fun onLogComplete() {
        //val userInfo = partialUserInfo.value!!
        uiScope.launch {
            /*
            database.userInfoDao().insert(
                UserInfoEntity(
                    name = userInfo.name!!,
                    fitnessLevel = userInfo.fitnessLevel!!,
                    totalLevelWorkoutTime = userInfo.totalLevelWorkoutTime!!,
                    birthDate = userInfo.birthDate!!,
                    height = userInfo.height!!,
                    weight = userInfo.weight!!,
                    targetWeight = userInfo.targetWeight!!,
                    gender = userInfo.gender!!
                )
            )

             */
            delay(2000)
            _navigateToMainPage.value = Event(true)
        }
    }

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}