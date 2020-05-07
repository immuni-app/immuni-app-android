package org.immuni.android.ui.uploaddata

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.extensions.livedata.Event
import kotlinx.coroutines.*
import org.immuni.android.data.FcmRepository
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.models.ExportData
import org.immuni.android.models.ExportDevice
import org.immuni.android.network.api.NetworkResource
import org.koin.core.KoinComponent

class UploadDataViewModel(
    val userId:String,
    val database: ImmuniDatabase,
    val repository: FcmRepository
) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val loading = MutableLiveData<Event<Boolean>>()
    val error = MutableLiveData<Event<Boolean>>()
    val success = MutableLiveData<Event<Boolean>>()

    fun exportData(code: String) {
        uiScope.launch {
            loading.value = Event(true)
            delay(500) // min loader time to avoid flickering
            val devices = database.bleContactDao().getAll().map {
                ExportDevice(
                    timestamp = it.timestamp.time / 1000.0,
                    btId = it.btId,
                    events = Base64.encodeToString(it.events, Base64.DEFAULT)
                )
            }
            /*
            val surveys = surveyManager.allHealthProfiles(userId).map {
                ExportHealthProfile.fromHealthProfile(it)
            }
             */

            val exportData = ExportData(
                profileId = userId,
                //surveys = surveys,
                devices = devices
            )
/*
            val result = repository.exportData(code, exportData)
            loading.value = Event(false)
            if (result is NetworkResource.Success) {
                success.value = Event(true)
            } else {
                error.value = Event(true)
            }

 */
        }
    }
}
