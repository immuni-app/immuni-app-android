package org.ascolto.onlus.geocrowd19.android.ui.onboarding

import androidx.lifecycle.*
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.utils.ExternalLinksHelper
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.picoMetrics.OnboardingCompleted
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class OnboardingViewModel(val handle: SavedStateHandle, private val database: AscoltoDatabase) :
    ViewModel(), KoinComponent {

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val onboarding: Onboarding by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val pico: Pico by inject()
    private val apiManager: ApiManager by inject()
    //private val geolocationManager: GeolocationManager by inject()

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
        if (handle.get<Serializable>(STATE_KEY) != null) {
            partialUserInfo.value = handle.get<Serializable>(STATE_KEY) as OnboardingUserInfo
        } else partialUserInfo.value = OnboardingUserInfo()

        partialUserInfo.addSource(savedStateLiveData) {
            partialUserInfo.value = it as? OnboardingUserInfo
        }

        /*uiScope.launch {
            geolocationManager.isActive.asFlow().drop(1).collect {
                _navigateToNextPage.value = Event(true)
            }
        }
         */
    }

    fun onPrivacyPolicyClick() {
        oracle.settings()?.privacyPolicyUrl?.let { url ->
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
            delay(2000) // keep here to allow smooth page transition

            val updatedMe = apiManager.updateMainUser(
                User(
                    id = "",
                    ageGroup = userInfo.ageGroup!!,
                    gender = userInfo.gender!!
                )
            )
            val isCompleted = updatedMe?.mainUser != null
            onboarding.setCompleted(isCompleted)
            if (isCompleted) {
                pico.trackEvent(OnboardingCompleted().userAction)
            }

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
        // TODO agreed no privacy policy API for now
        /*uiScope.launch {
            oracle.api.privacyNotice(PrivacyNoticeRequest(
                oracle.settings()?.privacyVersion ?: "",
                hashMapOf(),
                "unknown"
            ))
        }*/
        _navigateToNextPage.value = Event(true)
    }
}
