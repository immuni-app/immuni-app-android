package org.immuni.android.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.utils.retry
import kotlinx.coroutines.*
import org.immuni.android.managers.UserManager
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.welcome.Welcome
import org.immuni.android.util.Flags
import org.immuni.android.util.setFlag
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.IOException

class SetupViewModel(val repo: SetupRepository) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val setup: Setup by inject()
    private val onboarding: Onboarding by inject()
    private val welcome: Welcome by inject()
    private val userManager: UserManager by inject()

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

            delay(1000) // allow concierge to retrieve the async AAID

            // refresh oracle settings
            // set timeout here to allow the user to use the app offline
            // (this is not the very first startup that must to be blocking)
            withTimeoutOrNull(5000) {
                repo.getOracleSetting()
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
                        block = { repo.getOracleSetting() },
                        exitWhen = { result -> result.isSuccessful },
                        onIntermediateFailure = { errorDuringSetup.value = true }
                    )
                    if (!settings.isSuccessful) {
                        throw IOException()
                    } else {
                        errorDuringSetup.value = false
                    }

                    val me = repo.getOracleMe()
                    if (!me.isSuccessful) {
                        throw IOException()
                    }

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
        if (!welcome.isComplete()) _navigateToWelcome.value = Event(true)
        else if (!onboarding.isComplete()) _navigateToOnboarding.value = Event(true)
        else _navigateToMainPage.value = Event(true)
    }

    private fun setAddFamilyMemberDialogShown() {
        setFlag(Flags.ADD_FAMILY_MEMBER_DIALOG_SHOWN, true)
    }

    companion object {
        const val TAG = "SetupViewModel"
    }
}
