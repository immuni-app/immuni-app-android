package org.immuni.android.managers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bendingspoons.oracle.Oracle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.koin.core.KoinComponent
import org.koin.core.inject

class PermissionsManager(val context: Context) : KoinComponent {

    val isActive = ConflatedBroadcastChannel<Boolean>(hasAllPermissions((context)))

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

        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName: String = context.packageName
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null) return pm.isIgnoringBatteryOptimizations(packageName)
            }
            return false
        }

        fun startChangeBatteryOptimizationSettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            }
        }

        fun startChangeBatteryOptimization(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    val packageName: String = context.packageName
                    data = Uri.parse("package:$packageName")
                }
                context.startActivity(intent)
            }
        }

        fun startChangeGlobalGeolocalisationSettings(activity: Activity) {
            val intent = Intent().apply {
                action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
            }
            activity.startActivity(intent)
        }

        var alreadyShownSystemGeolocationDialog = false
        fun startChangeGlobalGeolocalisation(activity: Activity, requestCode: Int): Boolean {

            if(alreadyShownSystemGeolocationDialog) {
                return false
            }

            alreadyShownSystemGeolocationDialog = true
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(LocationRequest.create())
            builder.setAlwaysShow(true)
            val client: SettingsClient = LocationServices.getSettingsClient(activity)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
                .addOnSuccessListener {}
                .addOnFailureListener { exception ->
                if (exception is ResolvableApiException){
                    try {
                        exception.startResolutionForResult(activity, requestCode)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
            return true
        }
    }

    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    fun requestPermissions(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, allPermissions.toTypedArray(), REQUEST_CODE)
    }

    fun shouldShowPermissions(activity: AppCompatActivity, vararg permissions: String): Boolean {
        return permissions.all {
            activity.shouldShowRequestPermissionRationale(it)
        }
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

    fun geolocationPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}
