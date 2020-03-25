package org.ascolto.onlus.geocrowd19.android.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.managers.AscoltoNotificationManager
import org.ascolto.onlus.geocrowd19.android.managers.ble.BLEAdvertiser
import org.ascolto.onlus.geocrowd19.android.managers.ble.BLEScanner
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeActivity
import org.ascolto.onlus.geocrowd19.android.ui.setup.SetupActivity

class BLEForegroundServiceWorker(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {
        const val BLE_CHANNLE = "BLE_CHANNEL"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        /*val inputUrl = inputData.getString(KEY_INPUT_URL)
            ?: return Result.failure()
        val outputFile = inputData.getString(KEY_OUTPUT_FILE_NAME)
            ?: return Result.failure()
         */
        // Mark the Worker as important
        //val progress = "Starting BLE"
        setForeground(createForegroundInfo())

        BLEAdvertiser().start()
        BLEScanner().start()
        while(true) {
            delay(10000)
        }
        //download(inputUrl, outputFile)
        return Result.success()
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(): ForegroundInfo {
        val title = "Immuni"
        val message = "Protezione di Immuni attiva!"

        // This PendingIntent can be used to cancel the worker
        // val intent = WorkManager.getInstance(applicationContext)
            //.createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notificationIntent = Intent(context, HomeActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(applicationContext, BLE_CHANNLE)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setTicker(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification_app)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            //.addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(100, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(BLE_CHANNLE, BLE_CHANNLE, importance)
            val androidNotificationManager = NotificationManagerCompat.from(context)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }
}