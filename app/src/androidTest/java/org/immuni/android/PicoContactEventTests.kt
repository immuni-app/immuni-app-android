package org.immuni.android

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.BLEEvent
import org.immuni.android.picoMetrics.BluetoothFoundPeripheralsSnapshot
import org.immuni.android.util.toJson
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class PicoContactEventTests {

    @Test
    fun testContactBase64() {

        val events = bleEvent1.toByteArray() + bleEvent2.toByteArray()

        val contacts = mutableListOf<BluetoothFoundPeripheralsSnapshot.Contact>()

        contacts.add(BluetoothFoundPeripheralsSnapshot.Contact(
            btId = "42d3s23sd332s",
            timestamp = 1587209359.0,
            events = Base64.encodeToString(events, Base64.DEFAULT)
        ))


        val json = toJson(contacts)
        assertEquals("[{\"bt_id\":\"42d3s23sd332s\",\"timestamp\":1.587209359E9,\"events\":\"/uy6gOy/\\n\"}]", json)
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
