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

package it.ministerodellasalute.immuni.logic.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.content.ContextCompat
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.main.MainActivity
import it.ministerodellasalute.immuni.ui.setup.SetupActivity
import java.util.*
import org.koin.core.KoinComponent

class AppNotificationManager(val context: Context) : KoinComponent {

    companion object {
        const val CHANNEL_ID = "exposure_notification"
        const val FOREGROUND_SERVICE_CHANNEL_ID = "fetch_teks_foreground_service"
        const val DEBUG_CHANNEL_ID = "debug_channel"
        private var DEBUG_ID = 0
        const val GROUP_ID = 20000
    }

    fun triggerNotification(type: NotificationType) {
        createExposureNotificationChannel()
        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        ).apply {
            setSmallIcon(R.drawable.ic_notification_app)
            setContentIntent(createPendingIntent())
        }
        when (type) {
            NotificationType.RiskReminder -> setupRiskReminderNotification(builder)
            NotificationType.OnboardingNotCompleted -> setupOnboardingNotCompletedNotification(
                builder
            )
            NotificationType.ForcedVersionUpdate -> setupForcedVersionUpdateNotification(builder)
            NotificationType.ServiceNotActive -> setupServiceNotActiveNotification(builder)
        }

        val androidNotificationManager = NotificationManagerCompat.from(context)
        androidNotificationManager.notify(type.id, builder.build().apply {
            flags = flags or Notification.FLAG_AUTO_CANCEL
        })
    }

    fun removeNotification(type: NotificationType) {
        val androidNotificationManager = NotificationManagerCompat.from(context)
        androidNotificationManager.cancel(type.id)
    }

    private fun setupRiskReminderNotification(builder: NotificationCompat.Builder) {
        val title = context.getString(R.string.notifications_risk_title)
        val message = context.getString(R.string.notifications_risk_description)
        builder.apply {
            setContentTitle(title)
            setContentText(message)
            color = ContextCompat.getColor(context, R.color.danger)
            priority = NotificationCompat.PRIORITY_HIGH
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }
    }

    private fun setupOnboardingNotCompletedNotification(builder: NotificationCompat.Builder) {
        val title = context.getString(R.string.notifications_update_os_title)
        val message = context.getString(R.string.notifications_update_os_description)
        builder.apply {
            setContentTitle(title)
            setContentText(message)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            priority = NotificationCompat.PRIORITY_HIGH
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            setContentIntent(createPendingOnboardingIntent())
        }
    }

    private fun setupForcedVersionUpdateNotification(builder: NotificationCompat.Builder) {
        val title = context.getString(R.string.notifications_update_app_title)
        val message = context.getString(R.string.notifications_update_app_description)

        val notificationIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            setPackage("com.android.vending")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder.apply {
            setContentTitle(title)
            setContentText(message)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            priority = NotificationCompat.PRIORITY_HIGH
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }
    }

    private fun setupServiceNotActiveNotification(builder: NotificationCompat.Builder) {
        val title = context.getString(R.string.notifications_not_active_service_title)
        val message = context.getString(R.string.notifications_not_active_service_description)
        builder.apply {
            setContentTitle(title)
            setContentText(message)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            priority = NotificationCompat.PRIORITY_HIGH
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }
    }

    private fun createPendingOnboardingIntent(): PendingIntent {
        val notificationIntent = Intent(context, SetupActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createPendingIntent(): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createExposureNotificationChannel() {
        val channelName = context.getString(R.string.exposure_notification_channel)
        createChannel(id = CHANNEL_ID, name = channelName)
    }

    private fun createForegroundServiceChannel() {
        val channelName = context.getString(R.string.app_name)
        createChannel(
            id = FOREGROUND_SERVICE_CHANNEL_ID, name = channelName, mute = true, showBadge = false,
            importance = IMPORTANCE_LOW
        )
    }

    private fun createDebugChannel() {
        val channelName = "Immuni - Debug"
        createChannel(
            id = DEBUG_CHANNEL_ID, name = channelName, mute = true, showBadge = false,
            importance = IMPORTANCE_LOW
        )
    }

    private fun createChannel(
        id: String,
        name: String,
        importance: Int? = null,
        mute: Boolean = false,
        showBadge: Boolean = true
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id,
                name,
                importance ?: NotificationManager.IMPORTANCE_DEFAULT
            )
            if (mute) channel.setSound(null, null)
            channel.setShowBadge(showBadge)
            val androidNotificationManager = NotificationManagerCompat.from(context)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }

    fun fetchKeysForegroundNotification(): Notification {
        createForegroundServiceChannel()
        return NotificationCompat.Builder(context, FOREGROUND_SERVICE_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setTicker(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.exposure_check_in_progress))
            .setSmallIcon(R.drawable.ic_notification_app)
            .setOnlyAlertOnce(true)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setOngoing(true)
            .setSound(null)
            .setVibrate(longArrayOf(0L))
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .build()
    }

    /**
     * Show debugging notifications in a unique notification group.
     */
    fun triggerDebugNotification(message: String) {
        createDebugChannel()

        val dateStr = DateFormat.getTimeFormat(context).format(Date())

        val groupBuilder = NotificationCompat.Builder(
            context, DEBUG_CHANNEL_ID
        ).apply {
            setSmallIcon(R.drawable.ic_notification_app)
            setContentTitle("Immuni - Debug")
            setContentText(message)
            setGroupSummary(true)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            setGroup(DEBUG_CHANNEL_ID)
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            setContentIntent(createPendingIntent())
        }

        val builder = NotificationCompat.Builder(
            context,
            DEBUG_CHANNEL_ID
        ).apply {
            setSmallIcon(R.drawable.ic_notification_app)
            setContentIntent(createPendingIntent())
            setContentTitle(dateStr)
            setContentText(message)
            setGroup(DEBUG_CHANNEL_ID)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            priority = NotificationCompat.PRIORITY_HIGH
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }

        val androidNotificationManager = NotificationManagerCompat.from(context)
        androidNotificationManager.notify(GROUP_ID, groupBuilder.build())
        androidNotificationManager.notify(DEBUG_ID++, builder.build().apply {
            flags = flags or Notification.FLAG_AUTO_CANCEL
        })
    }
}

enum class NotificationType(val id: Int) {
    RiskReminder(id = 20001),
    OnboardingNotCompleted(id = 20002),
    ForcedVersionUpdate(id = 20003),
    ServiceNotActive(id = 20004),
}
