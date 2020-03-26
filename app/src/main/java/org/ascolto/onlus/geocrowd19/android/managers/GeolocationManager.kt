package org.ascolto.onlus.geocrowd19.android.managers

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class GeolocationManager(val context: Context) : KoinComponent {

    companion object {
        const val REQUEST_CODE = 620
        val allPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        fun getNextPermissionToGrant(context: Context): String? {
            return allPermissions.find { !checkHasPermission(context, it) }
        }

        fun checkHasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
        }

        fun hasAllPermissions(context: Context): Boolean {
            return getNextPermissionToGrant(context) == null
        }

        fun globalLocalisationEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    //private val geoUniq: GeoUniq = get()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()

    val isActive = ConflatedBroadcastChannel<Boolean>(hasAllPermissions((context)))

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
        }
    }
}
