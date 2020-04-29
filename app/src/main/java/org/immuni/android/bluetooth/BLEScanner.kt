package org.immuni.android.bluetooth

import android.bluetooth.le.*
import android.os.ParcelUuid
import org.immuni.android.networking.Networking
import org.immuni.android.analytics.Pico
import kotlinx.coroutines.*
import org.immuni.android.networking.model.*
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.models.ProximityEvent
import org.immuni.android.metrics.BluetoothScanFailed
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject

class BLEScanner : KoinComponent {
    private val bluetoothManager: BluetoothManager by inject()
    private val database: ImmuniDatabase by inject()
    private val networking: Networking<ImmuniSettings, ImmuniMe> by inject()
    private val pico: Pico by inject()
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var myScanCallback = MyScanCallback()
    private val aggregator: ProximityEventsAggregator by inject()

    fun stop() {
        if (bluetoothManager.isBluetoothEnabled()) { // if not enabled, stopScan crashes
            bluetoothLeScanner?.stopScan(myScanCallback)
        }
        aggregator.stop()
    }

    suspend fun start(): Boolean {
        val scanMode = networking.settings()?.bleScanMode?.scanMode()
            ?: ScanSettings.SCAN_MODE_BALANCED
        log("Scan mode: ${networking.settings()?.bleScanMode}")

        if (!bluetoothManager.isBluetoothEnabled()) return false
        bluetoothLeScanner = bluetoothManager.adapter()?.bluetoothLeScanner
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
                setReportDelay(0)
                setScanMode(scanMode)
            }.build(),
            myScanCallback
        )
        aggregator.start()
        return true
    }

    private fun processResults(results: List<ScanResult>) {

        val encounters = mutableListOf<ProximityEvent>()
        results.forEach { result ->

            val serviceId = ParcelUuid.fromString(CGAIdentifiers.ServiceDataUUIDString)
            val bytesData = result.scanRecord?.serviceData?.get(serviceId)
            val rssi = result.rssi
            val txPower = result.scanRecord?.txPowerLevel ?: 0
            bytesData?.let { bytes ->
                val scannedBtId = byteArrayToHex(bytes)
                scannedBtId?.let { btId ->
                    encounters.add(
                        ProximityEvent(
                            btId = btId,
                            txPower = txPower,
                            rssi = rssi
                        )
                    )
                }
            }
        }

        aggregator.addProximityEvents(encounters)
    }

    inner class MyScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            processResults(results)

            log("onScanResult count=${results.size}")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            processResults(listOf(result))

            log("onScanResult count=1")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            log("onScanFailed $errorCode")
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