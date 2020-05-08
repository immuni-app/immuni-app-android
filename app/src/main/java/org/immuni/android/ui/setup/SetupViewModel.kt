package org.immuni.android.ui.setup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.IOException
import kotlinx.coroutines.*
import org.immuni.android.data.SettingsRepository
import org.immuni.android.extensions.livedata.Event
import org.immuni.android.extensions.utils.retry
import org.immuni.android.managers.UserManager
import org.immuni.android.network.api.NetworkResource
import org.koin.core.KoinComponent

class SetupViewModel(
    val userManager: UserManager,
    val repository: SettingsRepository
) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToOnboarding = MutableLiveData<Event<Boolean>>()
    val navigateToWelcome = MutableLiveData<Event<Boolean>>()
    val errorDuringSetup = MutableLiveData<Boolean>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun cancelInitializeJob() {
        initializeJob?.cancel()
    }

    var initializeJob: Job? = null
    fun initializeApp() {
        initializeJob?.cancel()
        initializeJob = uiScope.launch {

            // refresh oracle settings
            // set timeout here to allow the user to use the app offline
            // (this is not the very first startup that must to be blocking)
            withTimeoutOrNull(5000) {
                repository.fetchSettings()
            }

            if (true || userManager.isSetupComplete()) {
                delay(2000)
                navigateTo()
            } else {
                try {
                    // cleanup db
                    userManager.setSetupCompleted(false)

                    // the first time the call to settings and me is blocking, you cannot proceed without
                    val settings = retry(
                        times = 6,
                        block = { repository.fetchSettings() },
                        exitWhen = { result -> result is NetworkResource.Success },
                        onIntermediateFailure = { errorDuringSetup.value = true }
                    )

                    when (settings) {
                        is NetworkResource.Error -> throw IOException()
                    }

                    errorDuringSetup.value = false

                    // check all is ok
                    userManager.setSetupCompleted(true)

                    navigateTo()
                } catch (e: Exception) {
                    e.printStackTrace()
                    userManager.setSetupCompleted(false)
                    errorDuringSetup.value = true
                }
            }
        }
    }

    private fun navigateTo() {
        if (!userManager.isWelcomeComplete() || !userManager.isOnboardingComplete()) {
            navigateToWelcome.value = Event(true)
        } else {
            navigateToMainPage.value = Event(true)
        }
    }
}
