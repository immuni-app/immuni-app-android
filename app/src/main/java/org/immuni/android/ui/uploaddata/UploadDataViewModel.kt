package org.immuni.android.ui.uploaddata

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.base.livedata.Event
import org.immuni.android.analytics.Pico
import kotlinx.coroutines.*
import org.immuni.android.api.ImmuniAPIRepository
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyManager
import org.immuni.android.models.ExportData
import org.immuni.android.models.ExportDevice
import org.immuni.android.metrics.DataUploaded
import org.immuni.android.networking.api.NetworkResource
import org.koin.core.KoinComponent
import org.koin.core.inject

class UploadDataViewModel(val userId:String, val database: ImmuniDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val surveyManager: SurveyManager by inject()
    private val immuniAPIRepository: ImmuniAPIRepository by inject()
    private val pico: Pico by inject()

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

            val result = immuniAPIRepository.exportData(code, exportData)
            loading.value = Event(false)
            if (result is NetworkResource.Success) {
                success.value = Event(true)
                pico.trackEvent(DataUploaded(code).userAction)
            } else {
                error.value = Event(true)
            }
        }
    }
}
