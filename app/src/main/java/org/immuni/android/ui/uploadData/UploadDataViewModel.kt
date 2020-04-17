package org.immuni.android.ui.uploadData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.managers.SurveyManager
import org.immuni.android.models.ExportData
import org.immuni.android.models.ExportDevice
import org.immuni.android.models.ExportHealthProfile
import org.immuni.android.picoMetrics.DataUploaded
import org.koin.core.KoinComponent
import org.koin.core.inject

class UploadDataViewModel(val userId:String, val database: ImmuniDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val surveyManager: SurveyManager by inject()
    private val apiManager: ApiManager by inject()
    private val pico: Pico by inject()

    val loading = MutableLiveData<Event<Boolean>>()
    val error = MutableLiveData<Event<Boolean>>()
    val success = MutableLiveData<Event<Boolean>>()

    fun exportData(code: String) {
        uiScope.launch {
            loading.value = Event(true)
            delay(500) // min loader time to avoid flickering
//            val devices = database.bleContactDao().getAll().map { // FIXME
//                ExportDevice(
//                    timestamp = it.timestamp.time / 1000.0,
//                    btId = it.btId,
//                    rssi = it.rssi,
//                    txPower = it.txPower
//                )
//            }
            val devices = listOf<ExportDevice>()
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

            val result = apiManager.exportData(code, exportData)
            loading.value = Event(false)
            if (result.isSuccessful) {
                success.value = Event(true)
                pico.trackEvent(DataUploaded(code).userAction)
            } else {
                error.value = Event(true)
            }
        }
    }
}
