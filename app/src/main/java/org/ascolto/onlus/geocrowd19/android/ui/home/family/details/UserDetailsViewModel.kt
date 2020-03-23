package org.ascolto.onlus.geocrowd19.android.ui.home.family.details

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.models.User
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File

class UserDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()

    val user = MediatorLiveData<User>()

    init {
        uiScope.launch {
            oracle.meFlow().collect  {
                user.value = it.familyMembers
                    .union(listOf(it.mainUser))
                    .find { user -> user!!.id == userId }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}