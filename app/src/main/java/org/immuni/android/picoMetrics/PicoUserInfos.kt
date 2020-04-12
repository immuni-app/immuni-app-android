package org.immuni.android.picoMetrics

import PushNotificationState
import PushNotificationUtils
import android.Manifest
import android.os.Build
import com.squareup.moshi.Json
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.PermissionsManager
import org.koin.core.KoinComponent
import org.koin.core.inject

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

    fun userInfo() = "location_permission_level" to this

    companion object : KoinComponent {
        val PERMISSIONS_MANAGER: PermissionsManager by inject()

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

    fun userInfo() = "push_permission_level" to this

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
