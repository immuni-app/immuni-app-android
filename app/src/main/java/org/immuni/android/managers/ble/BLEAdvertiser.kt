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
    val bluetoothManager: BluetoothManager by inject()
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    val btId = oracle.me()?.btId!!
    lateinit var advertiser: BluetoothLeAdvertiser
    var callback = MyAdvertiseCallback()

    val id = Random.nextInt(0, 1000)

    fun stop() {
        advertiser.stopAdvertising(callback)
    }

    fun start() {
        val adapter = bluetoothManager.adapter() ?: return
        if (!adapter.isEnabled) adapter.enable()
        advertiser = adapter.bluetoothLeAdvertiser
        val builder = AdvertiseSettings.Builder()
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        builder.setConnectable(false)
        builder.setTimeout(0) // timeout max 180000 milliseconds
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.setIncludeDeviceName(false) // if true fail = 1
        val serviceid = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString) //btId
        val bytesMan = btId.replace("-", "")//byteArrayOf(1)
        val data = Hex.stringToBytes(bytesMan)
        dataBuilder.addServiceData(serviceid, data)
        dataBuilder.addServiceUuid(serviceid)
        dataBuilder.setIncludeTxPowerLevel(true)
        advertiser.startAdvertising(
            builder.build(), dataBuilder.build(),
            callback
        )
    }

    inner class MyAdvertiseCallback: AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d("ADVERTISER", "### FAILURE START ADVERTISER id=$id error = $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("ADVERTISER", "### SUCCESS START ADVERTISER id=$id")
        }
    }
}