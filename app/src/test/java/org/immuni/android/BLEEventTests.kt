package org.immuni.android

import org.immuni.android.db.entity.BLEEvent
import org.junit.Test

class BLEEventTests {
    @Test
    fun `test BLEEvent encoding`() {
        val time = 254
        val rssi = -70
        val txPower = -20
        val entity = BLEEvent(relativeTimestamp = time, rssi = rssi, txPower = txPower)
        val encoded = entity.toByteArray()
        val decoded = BLEEvent(byteArray = encoded)
        assert(entity == decoded)
    }

    @Test
    fun `test BLEEvent dao`() {
        val time = 254
        val rssi = -70
        val txPower = -20
        val entity = BLEEvent(relativeTimestamp = time, rssi = rssi, txPower = txPower)
        val encoded = entity.toByteArray()
        val decoded = BLEEvent(byteArray = encoded)
        assert(entity == decoded)
    }
}
