package org.immuni.android.managers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.immuni.android.networking.Networking
import org.immuni.android.R
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.ui.home.HomeActivity
import org.koin.core.KoinComponent
import org.koin.core.inject

class AppNotificationManager(val context: Context): KoinComponent {

    private val networking: Networking<ImmuniSettings> by inject()

    val WARNING_NOTIFICATION_ID = 200001

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library

    fun createChannel(id: String,
                      name: String,
                      importance: Int? = null,
                      mute: Boolean = false,
                      showBadge: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(name,
                name,
                importance ?: NotificationManager.IMPORTANCE_DEFAULT)
            if(mute) channel.setSound(null, null)
            channel.setShowBadge(showBadge)
            val androidNotificationManager = NotificationManagerCompat.from(context)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }

    fun removeWarningNotification() {
        val androidNotificationManager = NotificationManagerCompat.from(context)
        androidNotificationManager.cancel(WARNING_NOTIFICATION_ID)
    }

    fun triggerWarningNotification() {
        val channelId = "notificaton_channel_warning"
        val channelName = "Attenzione"
        createChannel(channelId, channelName, mute = true)

        // resume the app from its previous state (or open it if it's closed)
        val notificationIntent = Intent(context, HomeActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, channelName)
            .setSmallIcon(R.drawable.ic_notification_app)
            .setContentTitle("Immuni ha bisogno del tuo aiuto per funzionare")
            .setContentText("Apri Immuni e segui le istruzioni per permettere all’app di funzionare al meglio.")
            .setColor(ContextCompat.getColor(context, R.color.danger))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Apri Immuni e segui le istruzioni per permettere all’app di funzionare al meglio."))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val androidNotificationManager = NotificationManagerCompat.from(context)
        androidNotificationManager.notify(WARNING_NOTIFICATION_ID, builder.build())
    }

    fun createForegroundServiceNotification(): Notification {
        val channelId = "notificaton_channel_foreground_service"
        val channelName = "Immuni Servizio Attivo"
        createChannel(channelId, channelName, mute = true, showBadge = false)

        val title = "Immuni"
        val message = "Protezione di Immuni attiva!"

        val notificationIntent = Intent(context, HomeActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(context, channelName)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setTicker(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification_app)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            //.addAction(android.R.drawable.ic_delete, cancel, intent)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .build()

        return notification
    }

    fun triggerSurveyNotification() {
        /*
        val channelId = "notificaton_channel_survey"
        val name = context.getString(R.string.reminder_notification_channel_name)
        createChannel(channelId, name)

        val settings = oracle.settings() ?: return
        // resume the app from its previous state (or open it if it's closed)
        val notificationIntent = Intent(context, HomeActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val builder = NotificationCompat.Builder(
            context, SurveyNotificationManager.reminderNotificationChannelId
        )
            .setSmallIcon(R.drawable.ic_notification_app)
            .setContentTitle(settings.reminderNotificationTitle)
            .setContentText(settings.reminderNotificationMessage)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(settings.reminderNotificationMessage))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val androidNotificationManager = NotificationManagerCompat.from(context)
        androidNotificationManager.notify(SurveyNotificationManager.notificationId, builder.build())
         */
    }
}