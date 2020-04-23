package org.immuni.android

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.base.utils.toJson
import junit.framework.Assert.assertEquals
import org.immuni.android.db.entity.BLEEvent
import org.immuni.android.models.ExportDevice
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class BluetoothSerializationTests {
    @Test
    fun testExportDeviceSerialization() {
        val date = Date()
        val contact = ExportDevice(
            btId = "123",
            timestamp = date.time / 1000.0,
            events = Base64.encodeToString(bleEvent1.toByteArray() + bleEvent2.toByteArray(), Base64.DEFAULT)
        )

        val json = toJson(contact)
        val decoded = fromJson<ExportDevice>(json)
        assertEquals(contact, decoded)
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