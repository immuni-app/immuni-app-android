package org.immuni.android.ui.setup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.extensions.livedata.Event
import org.immuni.android.extensions.utils.retry
import kotlinx.coroutines.*
import org.immuni.android.api.APIManager
import org.immuni.android.managers.UserManager
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.welcome.Welcome
import org.koin.core.KoinComponent
import java.io.IOException

class SetupViewModel(
    val setup: Setup,
    val onboarding: Onboarding,
    val welcome: Welcome,
    val userManager: UserManager,
    val apiManager: APIManager
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
                apiManager.repository.settings()
            }

            if (setup.isComplete()) {
                delay(2000)
                navigateTo()
            } else {
                try {
                    // cleanup db
                    setup.setCompleted(false)

                    // the first time the call to settings and me is blocking, you cannot proceed without
                    val settings = retry(
                        times = 6,
                        block = { apiManager.repository.settings() },
                        exitWhen = { result -> result is NetworkResource.Success },
                        onIntermediateFailure = { errorDuringSetup.value = true }
                    )

                    when(settings) {
                        is NetworkResource.Error -> throw IOException()
                    }

                    errorDuringSetup.value = false

                    // check all is ok
                    setup.setCompleted(true)

                    if (userManager.familyMembers().isNotEmpty()) {
                        setAddFamilyMemberDialogShown()
                    }

                    navigateTo()

                } catch (e: Exception) {
                    e.printStackTrace()
                    setup.setCompleted(false)
                    errorDuringSetup.value = true
                }
            }
        }
    }

    private fun navigateTo() {
        if (!welcome.isComplete() || !onboarding.isComplete()) {
            navigateToWelcome.value = Event(true)
        } else {
            navigateToMainPage.value = Event(true)
        }
    }

    private fun setAddFamilyMemberDialogShown() {
        onboarding.setFamilyDialogShown(true)
    }
}
