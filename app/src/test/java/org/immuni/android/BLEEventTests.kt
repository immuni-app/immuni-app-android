package org.immuni.android

import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.BLEEvent
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class BLEEventTests {
    @Test
    fun `test BLEEvent encoding`() {
        val entity = bleEvent1
        val encoded = entity.toByteArray()
        val decoded = BLEEvent(byteArray = encoded)
        assert(entity == decoded)
    }

    @Test
    fun `test BLEContact equatable`() {
        val date = Date()
        val entity1 = BLEContactEntity(
            btId = "123",
            timestamp = date,
            events = bleEvent1.toByteArray() + bleEvent2.toByteArray()
        )
        val entity2 = BLEContactEntity(
            btId = "123",
            timestamp = date,
            events = bleEvent1.toByteArray() + bleEvent2.toByteArray()
        )
        val entity3 = BLEContactEntity(
            btId = "123",
            timestamp = date,
            events = bleEvent2.toByteArray()
        )
        assertEquals(entity1, entity2)
        assertNotEquals(entity1, entity3)
    }

    private val bleEvent1: BLEEvent
        get() {
            val time = 254
            val rssi = -70
            val txPower = -20
            return BLEEvent(relativeTimestamp = time, rssi = rssi, txPower = txPower)
        }

    private val bleEvent2: BLEEvent
        get() {
            val time = 128
            val rssi = -65
            val txPower = -20
            return BLEEvent(relativeTimestamp = time, rssi = rssi, txPower = txPower)
        }
}
