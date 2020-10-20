package it.ministerodellasalute.immuni.storeservices

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.jos.JosApps
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack
import com.huawei.updatesdk.service.otaupdate.UpdateKey
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
        /*JosApps.getAppUpdateClient(context).checkAppUpdate(context,
            object : CheckUpdateCallBack {
                override fun onMarketStoreError(code: Int) {
                }

                override fun onUpdateStoreError(code: Int) {
                }

                override fun onUpdateInfo(intent: Intent?) {
                    intent?.let {
                        val info = intent.getSerializableExtra(UpdateKey.INFO)
                        if (info is ApkUpgradeInfo) {
                            JosApps.getAppUpdateClient(context)
                                .showUpdateDialog(context, info, true)
                        }
                    }
                }

                override fun onMarketInstallInfo(intent: Intent?) {
                    intent?.let {

                    }
                }
            })*/
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
