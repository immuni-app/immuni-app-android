package org.immuni.android.managers.ble

import junit.framework.Assert.assertEquals
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.models.ProximityEvent
import org.junit.Test
import java.util.*

class ProximityEventsAggregatorTest {
    @Test
    fun `test proximity events rolling average`() {

        val rssis = listOf(-87, -87, -87, -87, -87, -91, -90, -82, -82, -81, -82, -81, -83)

        val average = rssis.fold(RssiRollingAverage()) { average, i ->
            average.newAverage(
                ProximityEvent(btId = "id", rssi = i, txPower = -21, date = Date())
            )
        }

        assertEquals(-85, average.contact.rssi)
    }
}
