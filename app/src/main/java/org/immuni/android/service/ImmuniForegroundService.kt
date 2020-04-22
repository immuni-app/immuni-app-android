package org.immuni.android.service

import PushNotificationUtils
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Base64
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.RELATIVE_TIMESTAMP_SECONDS
import org.immuni.android.managers.AppNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.bluetooth.BLEAdvertiser
import org.immuni.android.bluetooth.BLEScanner
import org.immuni.android.db.entity.SLOTS_PER_CONTACT_RECORD
import org.immuni.android.metrics.*
import org.immuni.android.metrics.BluetoothFoundPeripheralsSnapshot.Contact
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class ImmuniForegroundService : Service(), KoinComponent {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    enum class Actions {
        START,
        STOP
    }

    companion object: KoinComponent {
        const val FOREGROUND_NOTIFICATION_ID = 21032020
        const val PICO_LAST_SENT_EVENT_TIME = "PICO_LAST_SENT_EVENT_TIME"
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
    private val pico: Pico by inject()
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
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
            pico.trackEvent(ForegroundServiceDestroyed().userAction)
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
            pico.trackEvent(ForegroundServiceStarted().userAction)

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

        serviceScope.launch {
            pico.trackEvent(ForegroundServiceStopped().userAction)
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
                delay((oracle.settings()?.bleTimeoutSeconds?.toLong() ?: 180L) * 1000L)
            }
        }

        val periodicCheck = serviceScope.async {
            var previousPermissionsState = PermissionsState.OK
            while(isServiceStarted) {
                log("Periodic check....")
                // disable BLE from settings if needed
                if(oracle.settings()?.bleDisableAll == true) {
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

            if (!ImmuniApplication.lifecycleObserver.isInForeground) {
                pico.trackEvent(BackgroundPing().userAction)
            }
        }

        if (count % picoContactsUploadPeriodicity.div(PERIODICITY) == 0) {
            val lastSentEventTime = storage.load(PICO_LAST_SENT_EVENT_TIME, 0L)
            val currentTime =  Date().time
            var endTime = lastSentEventTime
            val timeWindow = RELATIVE_TIMESTAMP_SECONDS * SLOTS_PER_CONTACT_RECORD * 1000
            while ((endTime + timeWindow) < currentTime) {
                endTime += timeWindow
            }
            if (endTime == lastSentEventTime) {
                return
            }
            val newContacts =
                database.bleContactDao().getAllBetweenTimestamps(start = lastSentEventTime, end = endTime).map {
                    val contact = Contact(
                        btId = it.btId,
                        timestamp = it.timestamp.time / 1000.0,
                        events = Base64.encodeToString(it.events, Base64.DEFAULT)
                    )
                    contact
                }

            if(newContacts.isNotEmpty()) {
                pico.trackEvent(
                    BluetoothFoundPeripheralsSnapshot(
                        contacts = newContacts
                    ).userAction
                )
            }

            storage.save(PICO_LAST_SENT_EVENT_TIME, endTime)
        }
    }
}
