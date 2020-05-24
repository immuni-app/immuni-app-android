/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment

/**
 * Push notifications manager with utility methods.
 */
class PushNotificationManager(
    val context: Context
) {

    companion object {
        const val NOTIFICATIONS_SETTINGS_REQUEST = 980
    }

    /**
     * Returns true is push notifications are enabled for this app.
     */
    fun areNotificationsEnabled(): Boolean {
        return getPushNotificationState() == PushNotificationState.AUTHORIZED
    }

    /**
     * Returns the state of push notifications for this app.
     * If [PushNotificationState.PARTIAL] it means that some notification channel is disabled.
     *
     * @return [PushNotificationState]
     */
    fun getPushNotificationState(): PushNotificationState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!manager.areNotificationsEnabled()) {
                return PushNotificationState.DENIED
            }
            val channels = manager.notificationChannels

            when {
                channels.none { it.importance == NotificationManager.IMPORTANCE_NONE } -> {
                    PushNotificationState.AUTHORIZED
                }
                channels.all { it.importance == NotificationManager.IMPORTANCE_NONE } -> {
                    PushNotificationState.DENIED
                }
                else -> PushNotificationState.PARTIAL
            }
        } else {
            when (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                true -> PushNotificationState.AUTHORIZED
                false -> PushNotificationState.DENIED
            }
        }
    }

    fun openNotificationsSettings(context: Context, fragment: Fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            fragment.startActivityForResult(intent, NOTIFICATIONS_SETTINGS_REQUEST)
        } else {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            fragment.startActivityForResult(intent, NOTIFICATIONS_SETTINGS_REQUEST)
        }
    }
}

enum class PushNotificationState {
    DENIED, AUTHORIZED, PARTIAL
}
