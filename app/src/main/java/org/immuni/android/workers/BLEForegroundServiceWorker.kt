package org.immuni.android.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.delay
import org.immuni.android.AscoltoApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.model.AscoltoMe
import org.immuni.android.api.oracle.model.AscoltoSettings
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.managers.ble.BLEAdvertiser
import org.immuni.android.managers.ble.BLEScanner
import org.immuni.android.ui.home.HomeActivity
import org.immuni.android.ui.onboarding.Onboarding
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception

class BLEForegroundServiceWorker(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {

    val onboarding: Onboarding by inject()
    val bluetoothManager: BluetoothManager by inject()
    val btIdsManager: BtIdsManager by inject()

    companion object {
        const val TAG = "BLEForegroundServiceWorker"
        const val BLE_CHANNLE = "Immuni Servizio Attivo"
        const val notificationId = 121
        var currentAdvertiser: BLEAdvertiser? = null
        var currentScanner: BLEScanner? = null
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        // if the user didn't do the onboarding yet, not run the worker
        if(!onboarding.isComplete()) return Result.success()

        btIdsManager.setup()

        // cleanup current advertiser/scanner
        try {
            currentAdvertiser?.stop()
        } catch (e: Exception) { e.printStackTrace() }
        try {
            currentScanner?.stop()
        } catch (e: Exception) { e.printStackTrace() }

        setForeground(createForegroundInfo())

        currentAdvertiser = BLEAdvertiser(context).apply {
            start()
        }
        currentScanner = BLEScanner().apply {
            start()
        }
        while(true) {
            delay(5000)
        }
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
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            //.addAction(android.R.drawable.ic_delete, cancel, intent)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .build()

        return ForegroundInfo(notificationId, notification) //ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(BLE_CHANNLE, BLE_CHANNLE, importance)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            val androidNotificationManager = NotificationManagerCompat.from(context)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }
}