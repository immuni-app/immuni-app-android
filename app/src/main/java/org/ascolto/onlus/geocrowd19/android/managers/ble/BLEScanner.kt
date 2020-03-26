package org.ascolto.onlus.geocrowd19.android.managers.ble

import android.bluetooth.le.*
import android.content.res.TypedArray
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.db.entity.BLEContactEntity
import org.ascolto.onlus.geocrowd19.android.managers.BluetoothManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.nio.ByteBuffer
import kotlin.random.Random

class BLEScanner: KoinComponent {
    val bluetoothManager: BluetoothManager by inject()
    val database: AscoltoDatabase by inject()
    val id = Random.nextInt(0, 1000)
    lateinit var bluetoothLeScanner: BluetoothLeScanner
    var myScanCallback = MyScanCallback()

    fun stop() {
        bluetoothLeScanner.stopScan(myScanCallback)
    }

    fun start() {
        //bluetoothManager.adapter().startLeScan(leScanCallback)
        bluetoothLeScanner = bluetoothManager.adapter().bluetoothLeScanner
        val filter = listOf(ScanFilter.Builder().apply {
            val manData = byteArrayOf(1)
            val manMask = byteArrayOf(1)
            setManufacturerData(CGAIdentifiers.ManufacturerID, manData, manMask)
        }.build())
        bluetoothLeScanner.startScan(
            null,
            ScanSettings.Builder().apply {
                this.setReportDelay(5000)
            }.build(),
            myScanCallback
        )
    }

    private fun processResult(uuids: List<String>) {
        Log.d("SCAN RESULT", "### SCAN RESULT id=$id ${uuids.joinToString()}")
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

    inner class MyScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.map { it.scanRecord?.serviceUuids?.firstOrNull().toString() }?.toSet()
                ?.toList()?.let {
                processResult(it)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            processResult(listOf(result?.scanRecord?.serviceUuids?.firstOrNull().toString()))
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("SCAN RESULT", "### SCAN RESULT id=$id $errorCode")
        }
    }
}
