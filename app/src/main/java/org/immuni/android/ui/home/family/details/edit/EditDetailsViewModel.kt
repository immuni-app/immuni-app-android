package org.immuni.android.ui.home.family.details.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.models.*
import org.immuni.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject

class EditDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val apiManager: ApiManager by inject()
    val database: ImmuniDatabase by inject()

    val loading = MediatorLiveData<Boolean>()
    val navigateBack = MutableLiveData<Event<Boolean>>()
    val user = MediatorLiveData<User>()

    init {
        user()?.let {
            user.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun mainUser(): User {
        return oracle.me()?.mainUser!!
    }

    fun user(): User? {
        return mutableListOf<User>().apply {
            oracle.me()?.mainUser?.let { add(it) }
            oracle.me()?.familyMembers?.let { addAll(it) }
        }.firstOrNull { it.id == userId }
    }

    fun updateUser(user: User) {
        uiScope.launch {
            loading.value = true
            delay(500)
            val result = when {
                user.isMain -> apiManager.updateMainUser(user)
                else -> apiManager.updateExistingFamilyMember(userId, user)
            }
            if(result == null) {
                toast(ImmuniApplication.appContext.getString(R.string.server_generic_error))
            } else {
               navigateBack.value = Event(true)
            }
            loading.value = false
        }
    }

}