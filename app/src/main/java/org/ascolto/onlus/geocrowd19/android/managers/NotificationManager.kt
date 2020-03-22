package org.ascolto.onlus.geocrowd19.android.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.Oracle
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit


class AscoltoNotificationManager(private val context: Context) : KoinComponent {
    companion object {
        private const val workTag = "NotificationManager"
        const val reminderNotificationChannelId = "reminder"
    }

    val workManager = WorkManager.getInstance(context)
    val surveyManager: SurveyManager by inject()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminder_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(reminderNotificationChannelId, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun scheduleNext() {
        workManager.cancelAllWorkByTag(workTag)
        val notificationWork = OneTimeWorkRequestBuilder<NotifyWorker>()
        notificationWork.setInitialDelay(initialDelay(), TimeUnit.MILLISECONDS)
            .addTag(workTag)
        workManager.enqueue(notificationWork.build())
    }

    fun initialDelay(): Long {
        // schedule for later today if today's poll is not yet completed
        // schedule for tomorrow if it is
        return surveyManager.nextSurveyDate().time - Date().time
    }
}


class NotifyWorker(val context: Context, params: WorkerParameters) : Worker(context, params),
    KoinComponent {
    val notificationManager: AscoltoNotificationManager by inject()
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()

    override fun doWork(): Result { // Method to trigger an instant notification
        if (!AscoltoApplication.isForeground) {
            triggerNotification()
        }
        notificationManager.scheduleNext()
        return Result.success()
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private fun triggerNotification() {
        val settings = oracle.settings() ?: return
        val builder = NotificationCompat.Builder(
            context,
            AscoltoNotificationManager.reminderNotificationChannelId
        )
            //.setSmallIcon(R.drawable.notification_icon) // FIXME
            .setContentTitle(settings.reminderNotificationTitle)
            .setContentText(settings.reminderNotificationMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}
