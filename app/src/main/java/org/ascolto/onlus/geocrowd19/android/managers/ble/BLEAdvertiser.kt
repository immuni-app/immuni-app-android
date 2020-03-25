package org.ascolto.onlus.geocrowd19.android.managers.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import android.util.Log
import com.bendingspoons.oracle.Oracle
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.managers.BluetoothManager
import org.ascolto.onlus.geocrowd19.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject

class BLEAdvertiser: KoinComponent {

    fun start() {
        Advertiser().start()
    }
}

class Advertiser: KoinComponent {
    val bluetoothManager: BluetoothManager by inject()
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    val btId = oracle.me()?.btId!!
    fun start() {
        val adapter = bluetoothManager.adapter() ?: return
        if (!adapter.isEnabled) adapter.enable()
        val advertiser = adapter.bluetoothLeAdvertiser
        val builder = AdvertiseSettings.Builder()
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        builder.setConnectable(false)
        builder.setTimeout(0) // timeout max 180000 milliseconds
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.setIncludeDeviceName(false) // if true fail = 1
        val serviceid = ParcelUuid.fromString(btId)
        val bytesMan = byteArrayOf(1)
        dataBuilder.addManufacturerData(CGAIdentifiers.ManufacturerID, bytesMan)
        dataBuilder.addServiceUuid(serviceid)
        dataBuilder.setIncludeTxPowerLevel(true)
        advertiser.startAdvertising(
            builder.build(), dataBuilder.build(),
            object: AdvertiseCallback() {
                override fun onStartFailure(errorCode: Int) {
                    super.onStartFailure(errorCode)
                    Log.d("ADVERTISER", "### FAILURE START ADVERTISER error = $errorCode")
                }

                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    super.onStartSuccess(settingsInEffect)
                    Log.d("ADVERTISER", "### SUCCESS START ADVERTISER")
                }
            }
        )
    }
}