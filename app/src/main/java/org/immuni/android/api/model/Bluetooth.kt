package org.immuni.android.api.model

import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanSettings
import com.squareup.moshi.Json

enum class AdvertiseMode(val id: String) {
    @Json(name = "low_latency")
    ADVERTISE_MODE_LOW_LATENCY("low_latency"),
    @Json(name = "balanced")
    ADVERTISE_MODE_BALANCED("balanced"),
    @Json(name = "low_power")
    ADVERTISE_MODE_LOW_POWER("low_power");
}

enum class TxPowerLevel(val id: String) {
    @Json(name = "ultra_low")
    ADVERTISE_TX_POWER_ULTRA_LOW("ultra_low"),
    @Json(name = "low")
    ADVERTISE_TX_POWER_LOW("low"),
    @Json(name = "medium")
    ADVERTISE_TX_POWER_MEDIUM("medium"),
    @Json(name = "high")
    ADVERTISE_TX_POWER_HIGH("high");
}

enum class ScanMode(val id: String) {
    @Json(name = "opportunistic")
    SCAN_MODE_OPPORTUNISTIC("opportunistic"),
    @Json(name = "low_power")
    SCAN_MODE_LOW_POWER("low_power"),
    @Json(name = "balanced")
    SCAN_MODE_BALANCED("balanced"),
    @Json(name = "low_latency")
    SCAN_MODE_LOW_LATENCY("low_latency");
}

fun ScanMode.scanMode(): Int {
    return when (this) {
        ScanMode.SCAN_MODE_OPPORTUNISTIC -> ScanSettings.SCAN_MODE_OPPORTUNISTIC
        ScanMode.SCAN_MODE_LOW_POWER -> ScanSettings.SCAN_MODE_LOW_POWER
        ScanMode.SCAN_MODE_BALANCED -> ScanSettings.SCAN_MODE_BALANCED
        ScanMode.SCAN_MODE_LOW_LATENCY -> ScanSettings.SCAN_MODE_LOW_LATENCY
    }
}

fun AdvertiseMode.advertiseMode(): Int {
    return when (this) {
        AdvertiseMode.ADVERTISE_MODE_LOW_LATENCY -> AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
        AdvertiseMode.ADVERTISE_MODE_BALANCED -> AdvertiseSettings.ADVERTISE_MODE_BALANCED
        AdvertiseMode.ADVERTISE_MODE_LOW_POWER -> AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
    }
}

fun TxPowerLevel.txPowerLevel(): Int {
    return when (this) {
        TxPowerLevel.ADVERTISE_TX_POWER_ULTRA_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
        TxPowerLevel.ADVERTISE_TX_POWER_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_LOW
        TxPowerLevel.ADVERTISE_TX_POWER_MEDIUM -> AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
        TxPowerLevel.ADVERTISE_TX_POWER_HIGH -> AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
    }
}
