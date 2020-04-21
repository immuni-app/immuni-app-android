package org.immuni.android.metrics

import PushNotificationState
import PushNotificationUtils
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.ImmuniApplication
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Utility class to obtain app specific user info.
 */

class PicoUserInfos {

    companion object: KoinComponent {

        fun databaseSize(context: Context): Pair<String, Any> {
            return "bluetooth_database_size" to ImmuniDatabase.databaseSize(context)
        }

        fun batteryLevel(context: Context): Pair<String, Any> {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return "battery_level" to bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        fun bluetoothActive(): Pair<String, Any> {
            val btManager: BluetoothManager by inject()
            return "bluetooth_active" to when(btManager.isBluetoothEnabled()) {
                true -> "on"
                false -> "off"
            }
        }

        fun locationActive(context: Context): Pair<String, Any> {
            return "localization_active" to when(PermissionsManager.globalLocalisationEnabled(context)) {
                true -> "on"
                false -> "off"
            }
        }

        fun batteryOptimization(context: Context): Pair<String, Any> {
            return "battery_optimization_app_level" to when(PermissionsManager.isIgnoringBatteryOptimizations(context)) {
                true -> "whitelisted"
                false -> "non_whitelisted"
            }
        }

        fun locationPermissionsLevel(): Pair<String, Any> {
            return "location_permission_level" to LocationPermissionLevel.instance()
        }

        fun location(context: Context): Pair<String, Any> {
            var value: CurrentLocation = CurrentLocation(0.0, 0.0, 0.0)

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

            if (locationManager == null || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                value = CurrentLocation(0.0, 0.0, 0.0)
            } else {
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(location != null) {
                    value = CurrentLocation(
                        location.latitude,
                        location.longitude,
                        location.accuracy.toDouble())
                }
            }
            return "geolocation_position" to value
        }

        fun pushPermissionLevel(): Pair<String, Any> {
            return "push_permission_level" to PushPermissionLevel.instance()
        }
    }
}

enum class LocationPermissionLevel {
    // the level is not known yet (e.g., before the onboarding)
    @Json(name = "unknown")
    UNKNOWN,

    // for reasons outside the control of the user (e.g., parental control) the access is not possible
    @Json(name = "restricted")
    RESTRICTED,

    // the user has explicitly denied the permissions
    @Json(name = "denied")
    DENIED,

    // the user has authorized the location tracking both in foreground and background
    @Json(name = "alwaysAuthorized")
    ALWAYS_AUTHORIZED,

    // the user has allowed the app to use the location only in foreground
    @Json(name = "foregroundAuthorized")
    FOREGROUND_AUTHORIZED;

    companion object : KoinComponent {

        fun instance(): LocationPermissionLevel {
            val context = ImmuniApplication.appContext
            val hasAllPermissions = PermissionsManager.hasAllPermissions(context)
            val hasForegroundPermission = PermissionsManager.checkHasPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val hasBackgroundPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionsManager.checkHasPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
            return when {
                hasAllPermissions -> ALWAYS_AUTHORIZED
                hasForegroundPermission && !hasBackgroundPermission-> FOREGROUND_AUTHORIZED
                else -> DENIED
            }
        }
    }
}

enum class PushPermissionLevel {
    // the level is not known yet (e.g., before the onboarding)
    @Json(name = "unknown")
    UNKNOWN,

    // the user has explicitly denied the permissions
    @Json(name = "denied")
    DENIED,

    // the user has authorized push notifications
    @Json(name = "authorized")
    AUTHORIZED,

    // the user has enabled push notification partially (see specific OSs details)
    @Json(name = "partial")
    PARTIAL;

    companion object {
        fun instance(): PushPermissionLevel {
            val notificationState =
                PushNotificationUtils.getPushNotificationState(ImmuniApplication.appContext)
            return when (notificationState) {
                PushNotificationState.DENIED -> DENIED
                PushNotificationState.AUTHORIZED -> AUTHORIZED
                PushNotificationState.PARTIAL -> PARTIAL
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class CurrentLocation(
    @field:Json(name = "lat") val lat: Double,
    @field:Json(name = "lon") val lon: Double,
    @field:Json(name = "accuracy") val accuracy: Double
)
