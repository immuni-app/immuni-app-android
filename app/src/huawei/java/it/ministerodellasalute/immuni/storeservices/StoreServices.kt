package it.ministerodellasalute.immuni.storeservices

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.liveData
import com.huawei.hms.api.HuaweiApiAvailability
import it.ministerodellasalute.immuni.logic.forceupdate.StoreServicesClient

class StoreServices : StoreServicesClient {

    private val minVersion = 40100300

    override fun areServicesAvailable(context: Context) =
        HuaweiApiAvailability.getInstance()
            .isHuaweiMobileServicesAvailable(context, minVersion) == 0

    override fun getServicesUpdateStatus(context: Context): StoreServicesClient.ServicesStatus {
        return when (HuaweiApiAvailability.getInstance()
            .isHuaweiMobileServicesAvailable(context, minVersion)) {
            2 -> StoreServicesClient.ServicesStatus.UPDATE_REQUIRED
            1, 3, 9, 21 -> StoreServicesClient.ServicesStatus.NOT_AVAILABLE
            else -> StoreServicesClient.ServicesStatus.AVAILABLE
        }
    }

    override fun checkUpdate(context: Context) {
    }

    override fun getVersionInformation(context: Context) = liveData {
        val version = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                HuaweiApiAvailability.SERVICES_PACKAGE,
                0
            )
        )
        emit(version.toString())
    }
}
