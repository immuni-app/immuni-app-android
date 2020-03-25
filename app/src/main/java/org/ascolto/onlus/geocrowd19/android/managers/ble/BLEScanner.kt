package org.ascolto.onlus.geocrowd19.android.managers.ble

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
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

class BLEScanner: KoinComponent {
    val bluetoothManager: BluetoothManager by inject()
    val database: AscoltoDatabase by inject()

    fun start() {
        //bluetoothManager.adapter().startLeScan(leScanCallback)
        bluetoothManager.adapter().bluetoothLeScanner.startScan(
            listOf(ScanFilter.Builder().apply {
                var manData = byteArrayOf(1)
                var manMask = byteArrayOf(1)
                setManufacturerData(CGAIdentifiers.ManufacturerID, manData, manMask)
            }.build()),
            ScanSettings.Builder().apply {
                this.setReportDelay(5000)
            }.build(),
            object: ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                results?.map { it.scanRecord?.serviceUuids?.firstOrNull().toString() }?.toSet()?.toList()?.let {
                    processResult(it)
                }
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                processResult(listOf(result?.scanRecord?.serviceUuids?.firstOrNull().toString()))
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d("SCAN RESULT", "### SCAN RESULT $errorCode")
            }
        })
    }

    private fun processResult(uuids: List<String>) {
        Log.d("SCAN RESULT", "### SCAN RESULT ${uuids.joinToString()}")
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
}
