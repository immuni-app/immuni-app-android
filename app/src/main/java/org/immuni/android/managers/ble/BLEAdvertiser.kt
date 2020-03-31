package org.immuni.android.managers.ble

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import com.bendingspoons.oracle.Oracle
import com.google.android.gms.common.util.Hex
import org.immuni.android.api.oracle.model.AscoltoMe
import org.immuni.android.api.oracle.model.AscoltoSettings
import org.immuni.android.managers.BluetoothManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.random.Random

class BLEAdvertiser: KoinComponent {
    private val bluetoothManager: BluetoothManager by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val btId = oracle.me()?.btId!!
    private lateinit var advertiser: BluetoothLeAdvertiser
    private var callback = MyAdvertiseCallback()

    private val id = Random.nextInt(0, 1000)

    fun stop() {
        advertiser.stopAdvertising(callback)
    }

    fun start() {
        val adapter = bluetoothManager.adapter()
        if (!adapter.isEnabled) adapter.enable()

        advertiser = adapter.bluetoothLeAdvertiser

        val builder = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            setConnectable(false)
            setTimeout(0) // timeout max 180000 milliseconds
            setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        }

        val dataBuilder = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false) // if true fail = 1
            val serviceId = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString) //btId
            val bytesMan = btId.replace("-", "")//byteArrayOf(1)
            val data = Hex.stringToBytes(bytesMan)
            addServiceData(serviceId, data)
            addServiceUuid(serviceId)
            setIncludeTxPowerLevel(true)
        }

        advertiser.startAdvertising(
            builder.build(), dataBuilder.build(),
            callback
        )
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
}