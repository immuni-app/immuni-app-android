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
        val filter = listOf(/*ScanFilter.Builder().apply {
            val manData = byteArrayOf(1)
            val manMask = byteArrayOf(1)
            setManufacturerData(CGAIdentifiers.ManufacturerID, manData, manMask)
        }.build(),*/
            ScanFilter.Builder().apply {
                val serviceUuidString = CGAIdentifiers.ServiceDataUUIDString
                val serviceUuidMaskString = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"
                val parcelUuid = ParcelUuid.fromString(serviceUuidString)
                val parcelUuidMask = ParcelUuid.fromString(serviceUuidMaskString)
                setServiceUuid(parcelUuid, parcelUuidMask)
            }.build())
        bluetoothLeScanner.startScan(
            filter,
            ScanSettings.Builder().apply {
                this.setReportDelay(2000)
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
            val ret = mutableSetOf<String>()
            results?.forEach { result ->

                val serviceid = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString)
                Log.d("TAHAHA", "### " + byteArrayToHex(result.scanRecord?.serviceData?.get(serviceid)!!))

                result.scanRecord?.serviceUuids?.forEach { uuid ->
                    ret.add(uuid.toString())
                }
            }
            processResult(ret.toList())
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val serviceid = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString)
            Log.d("TAHAHA", "### " + byteArrayToHex(result.scanRecord?.serviceData?.get(serviceid)!!))
            val ret = mutableSetOf<String>()
            result.scanRecord?.serviceUuids?.forEach { uuid ->
                ret.add(uuid.toString())
            }
            processResult(ret.toList())
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("SCAN RESULT", "### SCAN RESULT id=$id $errorCode")
        }
    }
}

fun byteArrayToHex(a: ByteArray): String? {
    val sb = java.lang.StringBuilder(a.size * 2)
    for (b in a) sb.append(String.format("%02x", b))
    return sb.toString()
}