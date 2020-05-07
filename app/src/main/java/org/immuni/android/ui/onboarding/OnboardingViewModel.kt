package org.immuni.android.ui.onboarding

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.*
import org.immuni.android.extensions.livedata.Event
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import org.immuni.android.ImmuniApplication
import org.immuni.android.data.SettingsDataSource
import org.immuni.android.managers.ExposureNotificationManager
import org.immuni.android.managers.UserManager
import org.immuni.android.models.User
import org.immuni.android.ui.dialog.WebViewDialogActivity
import org.koin.core.KoinComponent
import java.io.Serializable

class OnboardingViewModel(
    val handle: SavedStateHandle,
    val settings: SettingsDataSource,
    val userManager: UserManager,
    val exposureNotificationManager: ExposureNotificationManager
) :
    ViewModel(), KoinComponent {

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val partialUserInfo = MediatorLiveData<OnboardingUserInfo>()
    val loading = MutableLiveData<Boolean>()

    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)

    val navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val permissionsChanged = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val onFinishPermissionsTutorial = MutableLiveData<Event<Boolean>>()

    init {
        // init
        if (handle.get<Serializable>(STATE_KEY) != null) {
            partialUserInfo.value = handle.get<Serializable>(STATE_KEY) as OnboardingUserInfo
        } else {
            val mainUser = userManager.mainUser()
            partialUserInfo.value = OnboardingUserInfo(
                gender = mainUser?.gender,
                ageGroup = mainUser?.ageGroup
            )
        }

        partialUserInfo.addSource(savedStateLiveData) {
            partialUserInfo.value = it as? OnboardingUserInfo
        }

        uiScope.launch {
            exposureNotificationManager.areExposureNotificationsEnabled.asFlow().drop(1).collect { active ->
                permissionsChanged.value = Event(true)
            }
        }
    }

    fun onFinishPermissionsTutorial() {
        onFinishPermissionsTutorial.value = Event(true)
    }

    fun onPrivacyPolicyClick() {
        settings.latestSettings()?.privacyPolicyUrl?.let { url ->
            openUrlInDialog(url)
        }
    }

    fun onTosClick() {
        settings.latestSettings()?.termsOfServiceUrl?.let { url ->
            openUrlInDialog(url)
        }
    }

    private fun openUrlInDialog(url: String) {
        val context = ImmuniApplication.appContext
        val intent = Intent(context, WebViewDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("url", url)
        }
        context.startActivity(intent)
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
            //loading.value = true
            //delay(250)

            val mainUser = User(
                ageGroup = userInfo.ageGroup!!,
                gender = userInfo.gender!!,
                isMain = true
            )
            userManager.addUser(mainUser)

            //loading.value = false
            userManager.setOnboardingCompleted(true)
            navigateToNextPage.value = Event(true)
        }
    }

    fun onEnterDonePage() {
        uiScope.launch {
            delay(2000)
            navigateToMainPage.value = Event(true)
        }
    }

    fun onNextTap() {
        navigateToNextPage.value = Event(true)
    }

    fun onPrevTap() {
        navigateToPrevPage.value = Event(true)
    }

    fun onPrivacyPolicyAccepted() {
        navigateToNextPage.value = Event(true)
    }

    fun startExposureNotification(activity: Activity) {
        uiScope.launch {
            exposureNotificationManager.optInAndStartExposureTracing(activity)
        }

    }
}
