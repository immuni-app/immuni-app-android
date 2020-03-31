package org.immuni.android.managers.ble

import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.db.AscoltoDatabase
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.managers.BluetoothManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.random.Random

class BLEScanner: KoinComponent {
    private val bluetoothManager: BluetoothManager by inject()
    private val database: AscoltoDatabase by inject()
    private val id = Random.nextInt(0, 1000)
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var myScanCallback = MyScanCallback()

    fun stop() {
        bluetoothLeScanner.stopScan(myScanCallback)
    }

    fun start() {
        bluetoothLeScanner = bluetoothManager.adapter().bluetoothLeScanner
        val filter = listOf(
            ScanFilter.Builder().apply {
                val serviceUuidString = CGAIdentifiers.ServiceDataUUIDString
                val serviceUuidMaskString = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"
                val parcelUuid = ParcelUuid.fromString(serviceUuidString)
                val parcelUuidMask = ParcelUuid.fromString(serviceUuidMaskString)
                setServiceUuid(parcelUuid, parcelUuidMask)
            }.build()
        )
        bluetoothLeScanner.startScan(
            filter,
            ScanSettings.Builder().apply {
                setReportDelay(2000)
            }.build(),
            myScanCallback
        )
    }

    private fun processResult(btIds: List<String>) {
        Log.d("BLEScanner", "### SCAN RESULT id=$id ${btIds.joinToString()}")
        GlobalScope.launch {
            database.bleContactDao().insert(
                *btIds.map {
                    BLEContactEntity(
                        btId = it
                    )
                }.toTypedArray()
            )
        }
    }

    inner class MyScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            processResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            processResults(listOf(result))
        }

        private fun processResults(results: List<ScanResult>) {
            val btIds = mutableSetOf<String>()
            results.forEach { result ->

                val serviceId = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString)
                val bytesData = result.scanRecord?.serviceData?.get(serviceId)
                bytesData?.let { bytes ->
                    val scannedBtId = byteArrayToHex(bytes)
                    scannedBtId?.let {
                        btIds.add(it)
                    }
                }
            }
            processResult(btIds.toList())
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("BLEScanner", "### onScanFailed id=$id $errorCode")
        }
    }
}

fun byteArrayToHex(a: ByteArray): String? {
    val sb = java.lang.StringBuilder(a.size * 2)
    for (b in a) sb.append(String.format("%02x", b))
    return sb.toString()
}