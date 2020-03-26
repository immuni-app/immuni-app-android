package org.ascolto.onlus.geocrowd19.android.ui.uploadData

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.managers.SurveyManager
import org.ascolto.onlus.geocrowd19.android.models.ExportData
import org.ascolto.onlus.geocrowd19.android.models.ExportDevice
import org.ascolto.onlus.geocrowd19.android.models.ExportHealthProfile
import org.ascolto.onlus.geocrowd19.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject

class UploadDataViewModel(val database: AscoltoDatabase) : ViewModel(), KoinComponent {
    private val surveyManager: SurveyManager by inject()
    private val apiManager: ApiManager by inject()

    fun exportData(userId: String, code: String) {
        GlobalScope.launch {
            val devices = database.bleContactDao().getAll().map {
                ExportDevice(
                    timestamp = it.timestamp,
                    btId = it.btId,
                    signalStrength = it.signalStrength
                )
            }
            val surveys = surveyManager.allHealthProfiles(userId).map {
                ExportHealthProfile.fromHealthProfile(it)
            }

            val exportData = ExportData(
                profileId = userId,
                surveys = surveys,
                devices = devices
            )

            val result = apiManager.exportData(code, exportData)
            if (result.isSuccessful) {
                toast("Dati caricati con successo!")
            } else {
                toast("Il codice inserito non è corretto. Riprova.")
            }
        }
    }
}
