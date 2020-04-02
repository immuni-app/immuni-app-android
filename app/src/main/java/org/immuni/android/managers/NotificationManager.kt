package org.immuni.android.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.ui.home.HomeActivity
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit


class ImmuniNotificationManager(private val context: Context) : KoinComponent {
    companion object {
        private const val workTag = "NotificationManager"
        const val reminderNotificationChannelId = "reminder"
        const val notificationId = 123456
    }

    private val workManager = WorkManager.getInstance(context)
    private val surveyManager: SurveyManager by inject()
    private val androidNotificationManager = NotificationManagerCompat.from(context)
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    init {
        createNotificationChannel()
        GlobalScope.launch {
            ImmuniApplication.isForeground.consumeEach {
                scheduleNext(fromActivity = true)
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminder_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(reminderNotificationChannelId, name, importance)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNext(fromActivity: Boolean) {
        // avoid scheduling notifications if onboarding is not completed
        if (surveyManager.allUsers().isEmpty()) {
            return
        }
        if (fromActivity && surveyManager.areAllSurveysLogged()) {
            androidNotificationManager.cancel(notificationId)
        }
        schedule(initialDelay())
    }

    fun scheduleMock() {
        androidNotificationManager.cancel(notificationId)
        schedule(3000)
    }

    fun triggerNotification() {
        val settings = oracle.settings() ?: return
        // resume the app from its previous state (or open it if it's closed)
        val notificationIntent = Intent(context, HomeActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val builder = NotificationCompat.Builder(
            context, ImmuniNotificationManager.reminderNotificationChannelId
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

        androidNotificationManager.notify(notificationId, builder.build())
    }

    private fun schedule(delay: Long) {
        GlobalScope.launch(Dispatchers.Main) {
            workManager.cancelAllWorkByTag(workTag)

            // let the previous worker stop before restarting it
            delay(2000)

            val notificationWork = OneTimeWorkRequestBuilder<NotifyWorker>().apply {
                setInitialDelay(delay, TimeUnit.MILLISECONDS).addTag(workTag)
            }
            workManager.enqueue(notificationWork.build())
        }
    }

    private fun initialDelay(): Long {
        return surveyManager.nextSurveyDate().time - Date().time
    }
}


class NotifyWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    private val notificationManager: ImmuniNotificationManager by inject()

    override suspend fun doWork(): Result { // Method to trigger an instant notification
        notificationManager.triggerNotification()
        delay(2000)
        notificationManager.scheduleNext(fromActivity = false)
        return Result.success()
    }

}
