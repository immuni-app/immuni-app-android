package com.bendingspoons.ascolto.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.ascolto.ui.onboarding.Onboarding
import com.bendingspoons.base.livedata.Event
import org.koin.core.KoinComponent
import kotlinx.coroutines.*
import org.koin.core.inject
import java.io.IOException

class SetupViewModel(val repo : SetupRepository) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val setup: Setup by inject()
    private val onboarding: Onboarding by inject()

    private val _navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToMainPage : LiveData<Event<Boolean>>
        get() = _navigateToMainPage

    private val _navigateToOnboarding = MutableLiveData<Event<Boolean>>()
    val navigateToOnboarding : LiveData<Event<Boolean>>
        get() = _navigateToOnboarding

    val errorDuringSetup = MutableLiveData<Boolean>()

    init {
        // auto retry when connection is available
        uiScope.launch {
            repeat(Int.MAX_VALUE) {
                delay(5000)
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

            // TODO REMOVE TRUE
            if(true || setup.isComplete()) {
                delay(2000)
                navigateTo()
            }
            else { // populate db from Emporium
                try {

                    // cleanup db
                    setup.setCompleted(false)

                    // the first time the call to settings is blocking, you cannot proceed without
                    val settings = repo.getOracleSetting()
                    if (!settings.isSuccessful) {
                        throw IOException()
                    }

                    // fill db with Emporium data
                    repo.populateDb()

                    // check all is ok
                    setup.setCompleted(true)

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
        if(onboarding.isComplete()) _navigateToMainPage.value = Event(true)
        else _navigateToOnboarding.value = Event(true)

        _navigateToOnboarding.value = Event(true)
    }

    companion object {
        const val TAG = "SetupViewModel"
    }
}
