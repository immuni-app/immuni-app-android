package org.immuni.android.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.immuni.android.R
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.managers.ble.BLEAdvertiser
import org.immuni.android.managers.ble.BLEScanner
import org.immuni.android.ui.home.HomeActivity
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject

enum class Actions {
    START,
    STOP
}

class ImmuniForegroundService : Service(), KoinComponent {

    companion object {
        const val BLE_CHANNLE = "Immuni Servizio Attivo"
        const val FOREGROUND_NOTIFICATION_ID = 21032020
        var currentAdvertiser: BLEAdvertiser? = null
        var currentScanner: BLEScanner? = null
    }

    val serviceScope = CoroutineScope(SupervisorJob())
    val btIdsManager: BtIdsManager by inject()

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The Immuni service has been created")
        val notification = createNotification()
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel("The Immuni service has been destroyed")
        log("The Immuni service has been destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        //stopService()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the Immuni foreground service task")
        isServiceStarted = true

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ImmuniForegroundService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        serviceScope.launch(Dispatchers.IO) {
            doWork()
            while (isServiceStarted) {
                delay(1 * 30 * 1000)
            }
            log("End of the loop for the Immuni service")
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
    }

    private suspend fun doWork() = coroutineScope {

        btIdsManager.setup() // blocking we need the bt_ids

        async {
            btIdsManager.scheduleRefresh()
        }

        async {
            // cleanup current scanner
            try {
                currentScanner?.stop()
            } catch (e: java.lang.Exception) { e.printStackTrace() }

            currentScanner = BLEScanner().apply {
                start()
            }
        }

        async {
            // cleanup current advertiser
            try {
                currentAdvertiser?.stop()
            } catch (e: java.lang.Exception) { e.printStackTrace() }
            currentAdvertiser = BLEAdvertiser(applicationContext).apply {
                start()
            }
        }

        repeat(Int.MAX_VALUE) {
            //log("foreground service ping $this@BLEForegroundServiceWorker")
            delay(5000)
        }
    }

    private fun createNotification():  Notification{
        val title = "Immuni"
        val message = "Protezione di Immuni attiva!"

        // This PendingIntent can be used to cancel the worker
        // val intent = WorkManager.getInstance(applicationContext)
        //.createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notificationIntent = Intent(applicationContext, HomeActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(applicationContext, BLE_CHANNLE)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setTicker(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification_app)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            //.addAction(android.R.drawable.ic_delete, cancel, intent)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .build()

        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                BLE_CHANNLE,
                BLE_CHANNLE, importance)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            val androidNotificationManager = NotificationManagerCompat.from(applicationContext)
            androidNotificationManager.createNotificationChannel(channel)
        }
    }
}