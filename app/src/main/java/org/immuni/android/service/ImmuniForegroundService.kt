package org.immuni.android.service

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.managers.AppNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.ble.BLEAdvertiser
import org.immuni.android.managers.ble.BLEScanner
import org.immuni.android.toast
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
        const val FOREGROUND_NOTIFICATION_ID = 21032020
        var currentAdvertiser: BLEAdvertiser? = null
        var currentScanner: BLEScanner? = null
    }

    val serviceScope = CoroutineScope(SupervisorJob())
    val btIdsManager: BtIdsManager by inject()
    val permissionsManager: PermissionsManager by inject()
    val bluetoothManager: BluetoothManager by inject()
    val appNotificationManager: AppNotificationManager by inject()

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    private var bleJob: Job? = null

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                if(bluetoothManager.isBluetoothEnabled()) {
                    serviceScope.launch {
                        bleJob?.cancel()
                        delay(2000)
                        bleJob = launch {
                            startBleLoop()
                        }
                    }
                }
            }
        }
    }

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
        val notification = appNotificationManager.createForegroundServiceNotification()
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        applicationContext.registerReceiver(mReceiver, filter)
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
                    acquire(Long.MAX_VALUE)
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

    private suspend fun startBleLoop() = coroutineScope {
        val scanner = async {
            // cleanup current scanner
            try {
                currentScanner?.let {
                    it.stop()
                    delay(3000)
                }
            } catch (e: java.lang.Exception) { e.printStackTrace() }

            currentScanner = BLEScanner().apply {
                start()
            }
        }

        val advertiser = async {
            // cleanup current advertiser
            try {
                currentAdvertiser?.let {
                    it.stop()
                    delay(3000)
                }
            } catch (e: java.lang.Exception) { e.printStackTrace() }
            currentAdvertiser = BLEAdvertiser(applicationContext).apply {
                start()
            }
        }
    }

    private suspend fun doWork() = coroutineScope {

        btIdsManager.setup() // blocking we need the bt_ids

        val refreshBtIds = async {
            btIdsManager.scheduleRefresh()
        }

        val ble = async {
            bleJob = launch {
                startBleLoop()
            }
        }

        /*
        val bleLoop = async {
            repeat(Int.MAX_VALUE) {
                val ble = async { startBleLoop() }
                delay(30 * 1000)
                ble.cancel()
                delay(2000)
            }
        }
         */

        val polling = async {
            repeat(Int.MAX_VALUE) {
                //log("foreground service ping $this@BLEForegroundServiceWorker")

                if(!PermissionsManager.hasAllPermissions(applicationContext) ||
                    !PermissionsManager.isIgnoringBatteryOptimizations(applicationContext) ||
                    !PermissionsManager.globalLocalisationEnabled(applicationContext) ||
                    !bluetoothManager.isBluetoothEnabled() ||
                    !PushNotificationUtils.areNotificationsEnabled(ImmuniApplication.appContext)) {
                    withContext(Dispatchers.Main) {
                        appNotificationManager.triggerWarningNotification()
                    }
                } else {
                    appNotificationManager.removeWarningNotification()
                }

                delay(5 * 1000)
            }
        }
    }
}