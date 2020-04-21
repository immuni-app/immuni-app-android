package org.immuni.android.ble

class CGAIdentifiers {
    companion object {
        var ManufacturerID = 0x004C

        var ManufacturerData = 0x81

        var ServiceDataID = 0x8385
        var ServiceDataIDString = "00008385-0000-1000-8000-00805F9B34FB"

        var ServiceDataUUID = 0x8385
        var ServiceDataUUIDString = "00008385-0000-1000-8000-00805F9B34FB"

        var UserIdZero = byteArrayOf(
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )
    }
}