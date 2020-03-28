package org.ascolto.onlus.geocrowd19.android.ui.home.family.details.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.models.*
import org.ascolto.onlus.geocrowd19.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject

class EditDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    val apiManager: ApiManager by inject()
    val database: AscoltoDatabase by inject()

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
                toast(AscoltoApplication.appContext.getString(R.string.server_generic_error))
            } else {
               navigateBack.value = Event(true)
            }
            loading.value = false
        }
    }

}