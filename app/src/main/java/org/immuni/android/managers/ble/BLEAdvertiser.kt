package org.immuni.android.managers.ble

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.google.android.gms.common.util.Hex
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.BLEContactEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.picoMetrics.BluetoothAdvertisingFailed
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.random.Random

class BLEAdvertiser(val context: Context): KoinComponent {
    private val gattServerTag = "GATT_SERVER"
    private val database: ImmuniDatabase by inject()
    private val bluetoothManager: BluetoothManager by inject()
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val pico: Pico by inject()
    private lateinit var advertiser: BluetoothLeAdvertiser
    private var callback = MyAdvertiseCallback()
    private val btIdsManager: BtIdsManager by inject()
    private val id = Random.nextInt(0, 1000)

    fun stop() {
        advertiser.stopAdvertising(callback)
        bluetoothGattServer?.close()
        bluetoothGattServer = null
    }

    suspend fun start(): Boolean {
        val adapter = bluetoothManager.adapter()
        //if (!(adapter?.isEnabled == true)) adapter?.enable()

        if(!bluetoothManager.isBluetoothEnabled()) return false
        advertiser = adapter!!.bluetoothLeAdvertiser

        startServer()

        startAdvertising()

        return true
    }

    private suspend fun startAdvertising() {
        val btId = btIdsManager.getOrFetchActiveBtId()
        val builder = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            setConnectable(true)
            setTimeout(0) // timeout max 180000 milliseconds
            setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
        }

        val serviceId = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString) //btId

        val dataBuilder = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false) // if true fail = 1
            addServiceUuid(serviceId)
            setIncludeTxPowerLevel(true)
        }
        val scanResponseBuilder = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false) // if true fail = 1
            val bytesMan = btId.id.replace("-", "")//byteArrayOf(1)
            val data = Hex.stringToBytes(bytesMan)
            addServiceData(serviceId, data)
        }
        advertiser.startAdvertising(
            builder.build(), dataBuilder.build(), scanResponseBuilder.build(),
            callback
        )
        // wait until the bt id expires
        delay((btId.expirationTimestamp * 1000.0).toLong() - btIdsManager.correctTime())

        // wait a bit longer if the bt id is not yet expired
        while (btIdsManager.isNotExpired(btId)) {
            delay(1000)
        }

        // stop and start again
        advertiser.stopAdvertising(callback)

        startAdvertising()
    }

    inner class MyAdvertiseCallback: AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            log("Failure starting BLEAdvertiser id=$id error = $errorCode")
            GlobalScope.launch {
                pico.trackEvent(BluetoothAdvertisingFailed().userAction)
            }
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            log("### Success starting BLEAdvertiser id=$id")
        }
    }

    private fun startServer() {
        if (bluetoothGattServer != null) {
            return
        }
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val uuid = UUID.fromString(CGAIdentifiers.ServiceDataUUIDString)
        val service = BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val property = BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE
        val permission = BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
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
        GlobalScope.launch {
            database.bleContactDao().insert(
                    BLEContactEntity(
                        btId = uuid,
                        txPower = txPower,
                        rssi = rssi
                    )
            )
        }
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
                bluetoothGattServer?.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0, null)
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

            bluetoothGattServer?.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0, data)
        }
    }

    fun getPacketData(array: ByteArray): Triple<Int, Int, String>? {
        array.asList().let {
            if(it.size < 3) {
                log("RECEIVED INVALID GATT SERVER WRITE REQUEST")
                return null
            }
            return Triple(
                it.subList(0, 1)[0].toInt(),
                it.subList(1, 2)[0].toInt(),
                byteArrayToHex(it.subList(2, it.size).toByteArray()) ?: "")
        }
    }
}

