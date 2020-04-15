package org.immuni.android.managers.ble

import android.bluetooth.le.*
import android.os.ParcelUuid
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import de.fraunhofer.iis.Estimator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.picoMetrics.BluetoothScanFailed
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.random.Random

class BLEScanner: KoinComponent {
    private val bluetoothManager: BluetoothManager by inject()
    private val database: ImmuniDatabase by inject()
    private val btIdsManager: BtIdsManager by inject()
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val pico: Pico by inject()
    private val id = Random.nextInt(0, 1000)
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var myScanCallback = MyScanCallback()

    // Distance estimator
    private val distanceEstimator: Estimator by inject()

    fun stop() {
        bluetoothLeScanner?.stopScan(myScanCallback)
    }

    fun start(): Boolean {
        if(!bluetoothManager.isBluetoothEnabled()) return false
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
        bluetoothLeScanner?.startScan(
            filter,
            ScanSettings.Builder().apply {
                // with report delay the distance estimator doesn't work
                setReportDelay(5*1000) // 5 sec
                setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            }.build(),
            myScanCallback
        )

        return true
    }

    /*
    private fun calculateDistance(measurement: Measurement) {
        val list = distanceEstimator.push(measurement)
        list.forEach {
            log("Distance in meters between ${it.deviceId1} and ${it.deviceId2} = ${it.distance} meters")
        }
        /*val now = System.currentTimeMillis()
        storeResults(list.filter { now - it.timestamp < 60*1000 })
         */
    }

     */

    private fun storeResults(list: List<BLEContactEntity>) {

        // if in the same scan result we have the same ids, compute the avarage rssi
        val distinctAvaragedIds = list.groupingBy { it.btId }.reduce { id, avarage, contact ->
            BLEContactEntity(
                btId = contact.btId,
                txPower = contact.txPower,
                timestamp = contact.timestamp,
                rssi = (contact.rssi + avarage.rssi) / 2
            )
        }

        GlobalScope.launch {
            database.bleContactDao().insert(*distinctAvaragedIds.values.toTypedArray())
        }
    }

    private fun processResults(results: List<ScanResult>) {

        val encounters = mutableListOf<BLEContactEntity>()
        results.forEach { result ->

            val serviceId = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString)
            val bytesData = result.scanRecord?.serviceData?.get(serviceId)
            val rssi = result.rssi
            val txPower = result.scanRecord?.txPowerLevel ?: 0
            //val txPower = result.txPower // API 26+
            bytesData?.let { bytes ->
                val scannedBtId = byteArrayToHex(bytes)
                scannedBtId?.let { btId ->
                    encounters.add(
                        BLEContactEntity(
                            btId = btId,
                            txPower = txPower,
                            rssi = rssi
                        )
                    )

                    /*
                    calculateDistance(Measurement(
                        System.currentTimeMillis(),
                        rssi.toFloat(),
                        btIdsManager.getCurrentBtId()?.id ?: "",
                        btId,
                        ModelProvider.MOBILE_DEVICE.DEFAULT
                    ))
                     */
                }
            }
        }

        log("SCAN RESULT id=$id ${encounters.map { it.btId }.joinToString()}")
        storeResults(encounters)
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

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            log("onScanFailed id=$id $errorCode")
            GlobalScope.launch {
                pico.trackEvent(BluetoothScanFailed().userAction)
            }
        }
    }
}

fun byteArrayToHex(a: ByteArray): String? {
    val sb = java.lang.StringBuilder(a.size * 2)
    for (b in a) sb.append(String.format("%02x", b))
    return sb.toString()
}