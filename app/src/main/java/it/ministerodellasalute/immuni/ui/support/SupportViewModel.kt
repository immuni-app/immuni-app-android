package it.ministerodellasalute.immuni.ui.support

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.android.gms.common.GoogleApiAvailability
import it.ministerodellasalute.immuni.BuildConfig
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager

class SupportViewModel(
    val context: Context,
    val settingsManager: ConfigurationSettingsManager,
    val exposureManager: ExposureManager
) : ViewModel() {

    val settings = settingsManager.settings.value

    val contactSupportEmail = liveData {
        emit(settings.supportEmail)
    }

    val contactSupportPhone = liveData {
        emit(settings.supportPhone)
    }

    val supportWorkingHours = liveData {
        emit(settings.supportPhoneOpeningTime to settings.supportPhoneClosingTime)
    }

    val osVersion = liveData {
        emit("Android API ${Build.VERSION.SDK_INT}")
    }

    val deviceModel = liveData {
        emit("${Build.MANUFACTURER} ${Build.MODEL}")
    }

    val isExposureNotificationEnabled = liveData {
        val enabled = exposureManager.isBroadcastingActive.value
        emit(when(enabled){
            true -> "Attive"
            else -> "Non attive"
        })
    }

    val isBluetoothEnabled = liveData {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val enabled = bluetoothAdapter?.isEnabled ?: false
        emit(when(enabled){
            true -> "Attivo"
            false -> "Non attivo"
        })
    }

    val appVersion = liveData {
        val version = String.format(
            context.getString(R.string.settings_app_version),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
        emit(version)
    }
    val googlePlayVersion = liveData {
        val version = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                0
            )
        )
        emit(version.toString())
    }

    val connectionType = liveData {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isMetered = cm.isActiveNetworkMetered
        emit(when(isMetered){
            true -> "Mobile"
            false -> "Wi-Fi"
        })
    }
}
