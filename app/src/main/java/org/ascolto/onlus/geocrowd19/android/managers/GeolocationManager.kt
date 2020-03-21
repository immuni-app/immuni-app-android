package org.ascolto.onlus.geocrowd19.android.managers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.ascolto.onlus.geocrowd19.android.api.oracle.CustomOracleAPI
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import com.bendingspoons.oracle.Oracle
import com.geouniq.android.GeoUniq
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject

class GeolocationManager(val context: Context) : KoinComponent, GeoUniq.IDeviceIdListener {
    companion object {
        const val REQUEST_CODE = 620
        val allPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        fun getNextPermissionToGrant(context: Context): String? {
            return allPermissions.firstOrNull { checkHasPermission(context, it) }
        }

        fun checkHasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
        }

        fun hasAllPermissions(context: Context): Boolean {
            return getNextPermissionToGrant(context) == null
        }
    }

    private val geoUniq: GeoUniq = get()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()

    val isActive = ConflatedBroadcastChannel<Boolean>(hasAllPermissions((context)))
    val deviceId = ConflatedBroadcastChannel<String?>(null)

    init {
        geoUniq.enable()
        geoUniq.setDeviceIdListener(this)
        GlobalScope.launch {
            deviceId.consumeEach {
                it?.let {
                    // FIXME: add the proper call to Oracle one the endpoint is available
                    // oracle.customServiceAPI(CustomOracleAPI::class)
                }
            }
        }
        updatePrivacyConsents()
    }

    fun requestPermissions(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, allPermissions.toTypedArray(), REQUEST_CODE)
    }

    fun onRequestPermissionsResult(
        activity: AppCompatActivity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != REQUEST_CODE) {
            return
        }

        updateIsActive()
    }

    private fun updateIsActive() {
        GlobalScope.launch {
            isActive.send(hasAllPermissions(context))
            updatePrivacyConsents()
        }
    }

    private fun updatePrivacyConsents() {
        val isNowActive = isActive.value
        geoUniq.setPrivacyConsent(GeoUniq.ConsentItem.ANALYSIS, isNowActive)
        geoUniq.setPrivacyConsent(GeoUniq.ConsentItem.CUSTOMIZATION_AND_ADTARGETING, isNowActive)
    }

    // Device Id Listener
    override fun onDeviceIdAvailable(id: String?) {
        GlobalScope.launch {
            deviceId.send(id)
        }
    }
}
