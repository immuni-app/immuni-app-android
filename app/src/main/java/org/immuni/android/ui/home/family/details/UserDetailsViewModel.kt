package org.immuni.android.ui.home.family.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyManager
import org.immuni.android.models.*
import org.immuni.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val apiManager: ApiManager by inject()
    val surveyManager: SurveyManager by inject()
    val database: ImmuniDatabase by inject()

    val user = MediatorLiveData<User?>()
    val loading = MediatorLiveData<Boolean>()

    private val _navigateBack = MutableLiveData<Event<Boolean>>()
    val navigateBack: LiveData<Event<Boolean>>
        get() = _navigateBack

    init {
        uiScope.launch {
            oracle.meFlow().collect  {
                user.value = it.familyMembers
                    .union(listOf(it.mainUser))
                    .find { user -> user?.id == userId }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun deleteUser() {
        uiScope.launch {
            loading.value = true
            delay(500)
            val result = apiManager.deleteFamilyMember(userId)
            if(result == null) {
                toast(ImmuniApplication.appContext.getString(R.string.server_generic_error))
            } else {
                surveyManager.deleteUserData(userId)
                _navigateBack.value = Event(true)
            }
            loading.value = false
        }
    }
}