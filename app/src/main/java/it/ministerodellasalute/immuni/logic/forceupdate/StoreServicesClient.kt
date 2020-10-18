package it.ministerodellasalute.immuni.logic.forceupdate

import android.content.Context
import androidx.lifecycle.LiveData

interface StoreServicesClient {
    fun areServicesAvailable(context: Context): Boolean

    fun getServicesUpdateStatus(context: Context): ServicesStatus

    fun checkUpdate(context: Context)

    fun getVersionInformation(context: Context): LiveData<String>

    enum class ServicesStatus {
        NOT_AVAILABLE,
        UPDATE_REQUIRED,
        AVAILABLE
    }
}
