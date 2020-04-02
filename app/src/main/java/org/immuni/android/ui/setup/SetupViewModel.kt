package org.immuni.android.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.welcome.Welcome
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import org.koin.core.KoinComponent
import kotlinx.coroutines.*
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.util.Flags
import org.immuni.android.util.setFlag
import org.koin.core.inject
import java.io.IOException

class SetupViewModel(val repo: SetupRepository) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val setup: Setup by inject()
    private val onboarding: Onboarding by inject()
    private val welcome: Welcome by inject()
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

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

    init {
        // auto retry when connection is available
        uiScope.launch {
            repeat(Int.MAX_VALUE) {
                delay(10000)
                if (errorDuringSetup.value == true) {
                    initializeApp()
                }
            }
        }
    }

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
                    val settings = repo.getOracleSetting()
                    if (!settings.isSuccessful) {
                        throw IOException()
                    }

                    val me = repo.getOracleMe()
                    if (!me.isSuccessful) {
                        throw IOException()
                    }

                    // check all is ok
                    setup.setCompleted(true)

                    oracle.me()?.let {
                        if (it.familyMembers.isNotEmpty()) {
                            setAddFamilyMemberDialogShown()
                        }
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
