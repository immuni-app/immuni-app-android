package org.immuni.android.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import org.immuni.android.networking.Networking
import org.immuni.android.analytics.Pico
import com.google.android.gms.common.util.Hex
import kotlinx.coroutines.*
import org.immuni.android.BuildConfig
import org.immuni.android.networking.model.*
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.models.ProximityEvent
import org.immuni.android.metrics.BluetoothAdvertisingFailed
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception
import java.util.*

class BLEAdvertiser(val context: Context): KoinComponent {
    private val bluetoothManager: BluetoothManager by inject()
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val networking: Networking<ImmuniSettings, ImmuniMe> by inject()
    private val pico: Pico by inject()
    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback = MyAdvertiseCallback()
    private val btIdsManager: BtIdsManager by inject()
    private val aggregator: ProximityEventsAggregator by inject()

    var isAdvertising: Boolean = false
        private set

    private var advertisingJob: Job? = null

    fun stop() {
        stopAdvertise()
        bluetoothGattServer?.close()
        bluetoothGattServer = null
    }

    suspend fun start() = coroutineScope {
        val adapter = bluetoothManager.adapter()

        if (!bluetoothManager.isBluetoothEnabled()) return@coroutineScope
        advertiser = adapter?.bluetoothLeAdvertiser

        startServer()

        advertisingJob = launch {
            startAdvertising()
        }
    }

    private suspend fun startAdvertising() {

        val advertiseMode = networking.settings()?.bleAdvertiseMode?.advertiseMode()
            ?: AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
        log("Advertise mode: ${networking.settings()?.bleAdvertiseMode}")

        val txPowerLevel = networking.settings()?.bleTxPowerLevel?.txPowerLevel()
            ?: AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
        log("Tx Power Level mode: ${networking.settings()?.bleTxPowerLevel}")

        val btId = btIdsManager.getOrFetchActiveBtId()
        val builder = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(advertiseMode)
            setConnectable(true)
            setTimeout(0) // timeout max 180000 milliseconds
            setTxPowerLevel(txPowerLevel)
        }

        val serviceId = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString) //btId

        val dataBuilder = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false) // if true -> results in fail error = 1
            addServiceUuid(serviceId)
            setIncludeTxPowerLevel(true)
            val bytesMan = btId.id.replace("-", "")
            val data = Hex.stringToBytes(bytesMan)
            addServiceData(serviceId, data)
        }

        advertiser?.startAdvertising(
            builder.build(), dataBuilder.build(),// scanResponseBuilder.build(),
            callback
        )

        // lod waiting for btId expiration
        if(BuildConfig.DEBUG) {
            val log = coroutineScope {
                async {
                    repeat(Int.MAX_VALUE) {
                        delay(5000)
                        log("Advertiser waiting for btId expiration...")
                    }
                }
            }
        }

        // wait until the bt id expires
        delay((btId.expirationTimestamp * 1000.0).toLong() - btIdsManager.correctTime())

        // wait a bit longer if the bt id is not yet expired
        while (btIdsManager.isNotExpired(btId)) {
            delay(1000)
        }

        // stop and start again
        stopAdvertise()

        startAdvertising()
    }

    private fun stopAdvertise() {
        try {
            advertiser?.stopAdvertising(callback)
        } catch (e: Exception) {}
        advertisingJob?.cancel()
        isAdvertising = false
    }

    inner class MyAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false

            val reason = when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> {
                    isAdvertising = true
                    "advertise_failed_already_started"
                }
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "advertise_failed_feature_unsupported"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "advertise_failed_internal_error"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "advertise_failed_too_many_advertisers"
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "advertise_failed_data_too_large"
                else -> "unknown"
            }

            log("Failure starting BLEAdvertiser error = $errorCode reason=$reason")
            GlobalScope.launch {
                pico.trackEvent(BluetoothAdvertisingFailed(reason).userAction)
            }
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            log("### Success starting BLEAdvertiser")
            isAdvertising = true
        }
    }

    private fun startServer() {
        if (bluetoothGattServer != null) {
            return
        }
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val uuid = UUID.fromString(CGAIdentifiers.ServiceDataUUIDString)
        val service = BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val property =
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE
        val permission =
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        val characteristic = BluetoothGattCharacteristic(uuid, property, permission)
        service.addCharacteristic(characteristic)

        val result = bluetoothGattServer?.addService(service)

        if (result == null || result == false) {
            log("Unable to create GATT server")
        } else {
            log("GATT service added")
        }
    }

    private fun processResult(txPower: Int, rssi: Int, uuid: String) {
        aggregator.addProximityEvents(
            listOf(
                ProximityEvent(
                    txPower = txPower,
                    rssi = rssi,
                    btId = uuid
                )
            )
        )
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                log("BluetoothDevice CONNECTED: $device")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log("BluetoothDevice DISCONNECTED: $device")
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            log("Service added ${service?.uuid}, ${service?.characteristics?.first()?.uuid}")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            value?.let { array ->
                getPacketData(array)?.let {
                    val (txPower, rssi, uuid) = it
                    log("RECEIVED FROM GATT: txPower=$txPower rssi=$rssi uuid=$uuid")
                    processResult(txPower, rssi, uuid)
                }
            }

            if (responseNeeded) {
                bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0, null
                )
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {

            val bytesString = btIdsManager.getCurrentBtId()?.id?.replace("-", "") ?: ""
            val data = Hex.stringToBytes(bytesString)

            bluetoothGattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0, data
            )
        }
    }

    fun getPacketData(array: ByteArray): Triple<Int, Int, String>? {
        array.asList().let {
            if (it.size < 3) {
                log("RECEIVED INVALID GATT SERVER WRITE REQUEST")
                return null
            }
            return Triple(
                it.subList(0, 1)[0].toInt(),
                it.subList(1, 2)[0].toInt(),
                byteArrayToHex(it.subList(2, it.size).toByteArray()) ?: ""
            )
        }
    }
}

