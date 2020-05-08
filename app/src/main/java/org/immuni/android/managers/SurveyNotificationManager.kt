package org.immuni.android.managers

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.koin.core.KoinComponent
import org.koin.core.inject

class SurveyNotificationManager(private val context: Context) : KoinComponent {
    companion object {
        private const val workTag = "NotificationManager"
        const val reminderNotificationChannelId = "reminder"
        const val notificationId = 123456
    }

    private val workManager = WorkManager.getInstance(context)
    private val userManager: UserManager by inject()
    private val lifecycleObserver: AppLifecycleObserver by inject()
    private val appNotificationManager: AppNotificationManager by inject()
    private val androidNotificationManager = NotificationManagerCompat.from(context)

    init {
        GlobalScope.launch {
            lifecycleObserver.consumeEach {
                scheduleNext(fromActivity = true)
            }
        }
    }

    suspend fun scheduleNext(fromActivity: Boolean) {
        // avoid scheduling notifications if onboarding is not completed
        if (userManager.mainUser() == null) {
            return
        }
        if (false) {
            androidNotificationManager.cancel(notificationId)
        }
        schedule(initialDelay())
    }

    fun scheduleMock() {
        androidNotificationManager.cancel(notificationId)
        schedule(3000)
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
        return 0
    }

    fun triggerNotification() {
        appNotificationManager.triggerSurveyNotification()
    }
}

class NotifyWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    private val notificationManager: SurveyNotificationManager by inject()

    override suspend fun doWork(): Result { // Method to trigger an instant notification
        notificationManager.triggerNotification()
        delay(2000)
        notificationManager.scheduleNext(fromActivity = false)
        return Result.success()
    }
}
