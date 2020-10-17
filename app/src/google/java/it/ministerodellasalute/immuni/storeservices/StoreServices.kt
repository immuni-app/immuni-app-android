package it.ministerodellasalute.immuni.storeservices

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import it.ministerodellasalute.immuni.logic.forceupdate.StoreServicesClient

class StoreServices: StoreServicesClient {

    override fun areServicesAvailable(context: Context): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return status !in listOf(
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.SERVICE_UPDATING
        )
    }

    override fun getServicesStatus(context: Context): StoreServicesClient.ServicesStatus {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        val isUpdateRequired = status in listOf(
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.SERVICE_UPDATING
        )

        return if (isUpdateRequired) StoreServicesClient.ServicesStatus.UPDATE_REQUIRED else  StoreServicesClient.ServicesStatus.NOT_AVAILABLE
    }

    override fun checkUpdate(context: Context) {
        val playServicesPackage = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE
        PlayStoreActions.goToPlayStoreAppDetails(context, playServicesPackage)
    }

    override fun update() {
    }

    override fun getVersionInformation(context: Context) = liveData {
        val version = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                0
            )
        )
        emit(version.toString())
    }
}
