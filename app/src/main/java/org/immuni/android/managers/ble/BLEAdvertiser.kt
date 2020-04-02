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
import com.google.android.gms.common.util.Hex
import org.immuni.android.api.oracle.model.AscoltoMe
import org.immuni.android.api.oracle.model.AscoltoSettings
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.db.AscoltoDatabase
import org.immuni.android.db.entity.BLEContactEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.waitMillis
import org.immuni.android.managers.BtIdsManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.ext.getScopeName
import java.util.*
import kotlin.random.Random

class BLEAdvertiser(val context: Context): KoinComponent {
    private val gattServerTag = "GATT_SERVER"
    private val database: AscoltoDatabase by inject()
    private val bluetoothManager: BluetoothManager by inject()
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private lateinit var advertiser: BluetoothLeAdvertiser
    private var callback = MyAdvertiseCallback()
    private val btIdsManager: BtIdsManager by inject()
    private val id = Random.nextInt(0, 1000)

    fun stop() {
        advertiser.stopAdvertising(callback)
        bluetoothGattServer?.close()
        bluetoothGattServer = null
    }

    fun start() {
        val adapter = bluetoothManager.adapter()
        if (!adapter.isEnabled) adapter.enable()

        advertiser = adapter.bluetoothLeAdvertiser

        startServer()
        GlobalScope.launch {
            startAdvertising()
        }
    }

    private suspend fun startAdvertising() {
        val btId = btIdsManager.getOrFetchActiveBtId()
        val builder = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            setConnectable(false)
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

        GlobalScope.launch {
            startAdvertising()
        }
    }

    inner class MyAdvertiseCallback: AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d("BLEAdvertiser", "### FAILURE START BLEAdvertiser id=$id error = $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("BLEAdvertiser", "### SUCCESS START BLEAdvertiser id=$id")
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
        val characteristic = BluetoothGattCharacteristic(uuid,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE)
        service.addCharacteristic(characteristic)

        val result = bluetoothGattServer?.addService(service)

        if (result == null || result == false) {
            Log.d(gattServerTag, "### Unable to create GATT server")
        } else {
            Log.d(gattServerTag, "### GATT service added")
        }
    }

    private fun processResult(uuids: List<String>) {
        Log.d("SCAN RESULT", "### GATT SCAN RESULT id=$id ${uuids.joinToString()}")
        GlobalScope.launch {
            database.bleContactDao().insert(
                *uuids.map {
                    BLEContactEntity(
                        btId = it
                    )
                }.toTypedArray()
            )
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(gattServerTag, "### BluetoothDevice CONNECTED: $device")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(gattServerTag, "### BluetoothDevice DISCONNECTED: $device")
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            Log.d(gattServerTag, "### Service added ${service?.uuid}, ${service?.characteristics?.first()?.uuid}")
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
            value.let {
                val uuid = byteArrayToHex(it!!)
                if (uuid != null) {
                    Log.d(gattServerTag, "### RECEIVED UUID FROM GATT: $uuid")
                    processResult(listOf(uuid))
                } else {
                    Log.d(gattServerTag, "### RECEIVED INVALID UUID")
                }
            }

            if (responseNeeded) {
                bluetoothGattServer?.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0, null)
            }
        }
    }
}
