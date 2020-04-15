package org.immuni.android.service

import PushNotificationUtils
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.AppNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.ble.BLEAdvertiser
import org.immuni.android.managers.ble.BLEScanner
import org.immuni.android.picoMetrics.*
import org.immuni.android.picoMetrics.BluetoothFoundPeripheralsSnapshot.Contact
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

enum class Actions {
    START,
    STOP
}

class ImmuniForegroundService : Service(), KoinComponent {

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 21032020
        var currentAdvertiser: BLEAdvertiser? = null
        var currentScanner: BLEScanner? = null

        private const val PERIODICITY = 5
    }

    val serviceScope = CoroutineScope(SupervisorJob())
    val btIdsManager: BtIdsManager by inject()
    val bluetoothManager: BluetoothManager by inject()
    val appNotificationManager: AppNotificationManager by inject()
    private val pico: Pico by inject()
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val database: ImmuniDatabase by inject()

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    private var bleJob: Job? = null

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

        //val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        //applicationContext.registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        serviceScope.launch {
            pico.trackEvent(ForegroundServiceDestroyed().userAction)
            cancel("The Immuni service has been destroyed")
        }
        log("The Immuni service has been destroyed")
        super.onDestroy()
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
            pico.trackEvent(ForegroundServiceStarted().userAction)

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
            //applicationContext.unregisterReceiver(mReceiver)
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
    }

    private fun restartBlee() {
        serviceScope.launch {
            bleJob?.cancel()
            delay(2000)
            bleJob = launch {
                startBleLoop()
            }
        }
    }

    private suspend fun startBleLoop() = coroutineScope {
        val scanner = async {
            // cleanup current scanner
            try {
                currentScanner?.let {
                    it.stop()
                    delay(3000)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

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
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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

        val polling = async {
            var previousPermissionsState = PermissionsState.OK
            repeat(Int.MAX_VALUE) {
                //log("foreground service ping $this@BLEForegroundServiceWorker")

                val currentPermissionState: PermissionsState

                if(!PermissionsManager.hasAllPermissions(applicationContext) ||
                    !PermissionsManager.isIgnoringBatteryOptimizations(applicationContext) ||
                    !PermissionsManager.globalLocalisationEnabled(applicationContext) ||
                    !bluetoothManager.isBluetoothEnabled() ||
                    !PushNotificationUtils.areNotificationsEnabled(ImmuniApplication.appContext)) {
                    currentPermissionState = PermissionsState.MISSING
                    withContext(Dispatchers.Main) {
                        appNotificationManager.triggerWarningNotification()
                    }
                } else {
                    currentPermissionState = PermissionsState.OK
                    appNotificationManager.removeWarningNotification()
                }

                if(previousPermissionsState == PermissionsState.MISSING &&
                        currentPermissionState == PermissionsState.OK) {
                    restartBlee()
                }

                previousPermissionsState = currentPermissionState


                logEventsToPico()

                delay(PERIODICITY.toLong() * 1000)
            }
        }
    }

    enum class PermissionsState {
        MISSING,
        OK
    }

    private var picoCounter = 0
    private suspend fun logEventsToPico() {
        val count = picoCounter
        picoCounter += 1

        val picoPingPeriodicity = oracle.settings()?.picoPingPeriodicity ?: 30
        val picoContactsUploadPeriodicity = oracle.settings()?.picoContactsUploadPeriodicity ?: 60

        if (count % picoPingPeriodicity.div(PERIODICITY) == 0) {
            pico.trackEvent(ForegroundServiceRunning().userAction)

            if (!ImmuniApplication.isForeground.value) {
                pico.trackEvent(BackgroundPing().userAction)
            }
        }

        if (count % picoContactsUploadPeriodicity.div(PERIODICITY) == 0) {
            val thresholdTimestamp = Date().time - picoContactsUploadPeriodicity * 1000
            val newContacts =
                database.bleContactDao().getAllSinceTimestamp(thresholdTimestamp).map {
                    Contact(
                        btId = it.btId,
                        rssi = it.rssi,
                        txPower = it.txPower,
                        timestamp = it.timestamp.time / 1000.0
                    )
                }
            pico.trackEvent(
                BluetoothFoundPeripheralsSnapshot(
                    contacts = newContacts
                ).userAction
            )
        }
    }
}
