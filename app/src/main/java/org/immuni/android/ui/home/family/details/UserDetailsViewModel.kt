package org.immuni.android.ui.home.family.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.UserManager
import org.immuni.android.models.ExportData
import org.immuni.android.models.ExportDevice
import org.immuni.android.models.ExportHealthProfile
import org.immuni.android.models.User
import org.immuni.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val apiManager: ApiManager by inject()
    val userManager: UserManager by inject()
    val surveyManager: SurveyManager by inject()
    val database: ImmuniDatabase by inject()

    val user = MediatorLiveData<User?>()
    val loading = MediatorLiveData<Boolean>()

    private val _navigateBack = MutableLiveData<Event<Boolean>>()
    val navigateBack: LiveData<Event<Boolean>>
        get() = _navigateBack

    init {
        user.addSource(userManager.usersLiveData()) { users ->
            user.value = users.find { it.id == userId }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun deleteUser() {
        userManager.deleteUser(userId)
        surveyManager.deleteUserData(userId)
        _navigateBack.value = Event(true)
    }
}