package org.ascolto.onlus.geocrowd19.android.ui.home.family.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.managers.SurveyManager
import org.ascolto.onlus.geocrowd19.android.models.*
import org.ascolto.onlus.geocrowd19.android.toast
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File

class UserDetailsViewModel(val userId: String) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    val apiManager: ApiManager by inject()
    val surveyManager: SurveyManager by inject()
    val database: AscoltoDatabase by inject()

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
                toast(AscoltoApplication.appContext.getString(R.string.server_generic_error))
            } else {
                _navigateBack.value = Event(true)
            }
            loading.value = false
        }
    }

    fun exportData() {
        uiScope.launch {
            val devices = database.bleContactDao().getAll().map {
                ExportDevice(
                    timestamp = it.timestamp,
                    btId = it.btId,
                    signalStrength = it.signalStrength
                )
            }
            val surveys = surveyManager.allHealthProfiles(userId)
                .map { ExportHealthProfile.fromHealthProfile(it) }


            val exportData = ExportData(
                profileId = userId,
                surveys = surveys,
                devices = devices
            )

            apiManager.exportData("F4P3WLJY", exportData)
        }
    }
}