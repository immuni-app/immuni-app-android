package org.immuni.android.ui.home.family.details.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.UserManager
import org.immuni.android.models.User
import org.koin.core.KoinComponent
import org.koin.core.inject

class EditDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val userManager: UserManager by inject()
    val database: ImmuniDatabase by inject()

    val loading = MediatorLiveData<Boolean>()
    val navigateBack = MutableLiveData<Event<Boolean>>()
    val user = MediatorLiveData<User>()

    init {
        user.addSource(userManager.usersLiveData()) { users ->
            user.value = users.find { it.id == userId }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun mainUser(): User {
        return userManager.mainUser()!!
    }

    fun user(): User? {
        return userManager.user(userId)
    }

    fun updateUser(user: User) {
        uiScope.launch {
            //loading.value = true
            //delay(500)
            userManager.updateUser(user)
            navigateBack.value = Event(true)
            //loading.value = false
        }
    }
}
