package org.immuni.android.service

import PushNotificationUtils
import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.networking.Networking
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.AppNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.bluetooth.BLEAdvertiser
import org.immuni.android.bluetooth.BLEScanner
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniForegroundService : Service(), KoinComponent {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    enum class Actions {
        START,
        STOP
    }

    companion object: KoinComponent {
        const val FOREGROUND_NOTIFICATION_ID = 21032020
        var isServiceStarted = false
            private set

        private const val PERIODICITY = 5
    }

    private val advertiser: BLEAdvertiser by inject()
    private val scanner: BLEScanner by inject()
    private val btIdsManager: BtIdsManager by inject()
    private val bluetoothManager: BluetoothManager by inject()
    private val appNotificationManager: AppNotificationManager by inject()
    private val storage: KVStorage by inject()
    private val networking: Networking<ImmuniSettings, ImmuniMe> by inject()
    private val database: ImmuniDatabase by inject()

    override fun onBind(intent: Intent): IBinder? {
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
    }

    override fun onDestroy() {
        stopBle()
        serviceScope.launch {
            serviceScope.cancel("Service scope cancelling...")
            log("Service scope has been cancelled.")
        }

        log("The Immuni service has been destroyed")
        isServiceStarted = false
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
        log("Starting the foreground service task")
        isServiceStarted = true

        // keep the service running
        serviceScope.launch(Dispatchers.IO) {

            doWork()
            while (isServiceStarted) {
                delay(1 * 30 * 1000)
            }
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")

        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }

        isServiceStarted = false
    }

    private fun stopBle() {
        log("Stopping BLE")
        // stop current scanner
        scanner.stop()
        log("Stopped scanner")

        // stop current advertiser
        advertiser.stop()
        log("Stopped advertiser")
    }

    private suspend fun startBleLoop()  {
        stopBle()
        delay(3000)
        log("Starting BLE")
        val scanner = serviceScope.async {
            scanner.start()
            log("Started scanner")
        }

        val advertiser = serviceScope.async {
            advertiser.start()
            log("Started advertiser")
        }
    }

    private suspend fun doWork() {

        btIdsManager.setup() // blocking we need the bt_ids

        val refreshBtIds = serviceScope.async {
            btIdsManager.scheduleRefresh()
        }

        val bluetooth = serviceScope.async {
            while(isServiceStarted) {
                startBleLoop()
                delay((networking.settings()?.bleTimeoutSeconds?.toLong() ?: 180L) * 1000L)
            }
        }

        val periodicCheck = serviceScope.async {
            var previousPermissionsState = PermissionsState.OK
            while(isServiceStarted) {
                log("Periodic check....")
                // disable BLE from settings if needed
                if(networking.settings()?.bleDisableAll == true) {
                    stopService()
                    return@async
                }

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
                    log("Restarting BLE ads/scan/server.")
                    startBleLoop()
                }

                previousPermissionsState = currentPermissionState

                delay(PERIODICITY.toLong() * 1000)
            }
        }
    }

    enum class PermissionsState {
        MISSING,
        OK
    }
}
