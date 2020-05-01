package org.immuni.android.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.extensions.livedata.Event
import org.immuni.android.extensions.utils.retry
import kotlinx.coroutines.*
import org.immuni.android.api.ImmuniAPIRepository
import org.immuni.android.managers.UserManager
import org.immuni.android.networking.api.NetworkResource
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.welcome.Welcome
import org.immuni.android.util.Flags
import org.immuni.android.util.setFlag
import org.koin.core.KoinComponent
import java.io.IOException

class SetupViewModel(
    val setup: Setup,
    val onboarding: Onboarding,
    val welcome: Welcome,
    val userManager: UserManager,
    val repository: ImmuniAPIRepository
) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToMainPage: LiveData<Event<Boolean>>
        get() = _navigateToMainPage

    private val _navigateToOnboarding = MutableLiveData<Event<Boolean>>()
    val navigateToOnboarding: LiveData<Event<Boolean>>
        get() = _navigateToOnboarding

    private val _navigateToWelcome = MutableLiveData<Event<Boolean>>()
    val navigateToWelcome: LiveData<Event<Boolean>>
        get() = _navigateToWelcome

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
                repository.settings()
            }

            if (true || setup.isComplete()) {
                delay(2000)
                navigateTo()
            } else {
                try {
                    // cleanup db
                    setup.setCompleted(false)

                    // the first time the call to settings and me is blocking, you cannot proceed without
                    val settings = retry(
                        times = 6,
                        block = { repository.settings() },
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
            _navigateToWelcome.value = Event(true)
        } else {
            _navigateToMainPage.value = Event(true)
        }
    }

    private fun setAddFamilyMemberDialogShown() {
        setFlag(Flags.ADD_FAMILY_MEMBER_DIALOG_SHOWN, true)
    }
}
