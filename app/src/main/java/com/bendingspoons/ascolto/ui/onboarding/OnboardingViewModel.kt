package com.bendingspoons.ascolto.ui.onboarding

import androidx.lifecycle.*
import com.bendingspoons.ascolto.AscoltoApplication
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.utils.ExternalLinksHelper
import com.bendingspoons.oracle.api.model.PrivacyNoticeRequest
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable
import java.util.*

class OnboardingViewModel(val handle: SavedStateHandle, private val database: AscoltoDatabase) : ViewModel(), KoinComponent {

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val onboarding: Onboarding by inject()
    private val oracle: Oracle<AscoltoSettings> by inject()

    var partialUserInfo = MediatorLiveData<OnboardingUserInfo>()

    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)

    private val _navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToMainPage: LiveData<Event<Boolean>>
        get() = _navigateToMainPage

    private val _navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage: LiveData<Event<Boolean>>
        get() = _navigateToNextPage

    private val _navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage: LiveData<Event<Boolean>>
        get() = _navigateToPrevPage

    init {
        // init
        if(handle.get<Serializable>(STATE_KEY) != null) {
            partialUserInfo.value = handle.get<Serializable>(STATE_KEY) as OnboardingUserInfo
        } else partialUserInfo.value = OnboardingUserInfo()

        partialUserInfo.addSource(savedStateLiveData) {
            partialUserInfo.value = it as? OnboardingUserInfo
        }
    }

    fun onTosClick() {
        oracle.settings()?.tosUrl?.let { url ->
            ExternalLinksHelper.openLink(AscoltoApplication.appContext, url)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateUserInfo(newUserInfo: OnboardingUserInfo) {
        handle.set(STATE_KEY, newUserInfo)
    }

    fun userInfo(): OnboardingUserInfo? {
        return partialUserInfo.value
    }

    fun onOnboardingComplete() {
        val userInfo = partialUserInfo.value!!
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
            onboarding.setCompleted(true)
            delay(2000)
            _navigateToMainPage.value = Event(true)
        }
    }

    fun onNextTap() {
        _navigateToNextPage.value = Event(true)
    }

    fun onPrevTap() {
        _navigateToPrevPage.value = Event(true)
    }

    fun onPrivacyPolicyAccepted() {
        uiScope.launch {
            oracle.api.privacyNotice(PrivacyNoticeRequest(
                oracle.settings()?.privacyVersion ?: "",
                hashMapOf(),
                "unknown"
            ))
        }
        _navigateToNextPage.value = Event(true)
    }
}
