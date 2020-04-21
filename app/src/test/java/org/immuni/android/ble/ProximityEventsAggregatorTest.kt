package org.immuni.android.ble

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.models.ProximityEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class ProximityEventsAggregatorTest {

    @MockK(relaxed = true)
    lateinit var database: ImmuniDatabase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
    }

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

    @Test
    fun `test mutex avoid concurrent modification exception while aggregate data`() {
        runBlocking {
            val result = runCatching {
                // the aggregator start automatically aggregate every 1ms
                val aggregator = ProximityEventsAggregator(database, 1L)

                val tick = async {
                    aggregator.start()
                }

                val insert = async(Dispatchers.Default) {
                    for (i in 0..10000) {
                        aggregator.addProximityEvents(
                            listOf(
                                ProximityEvent(
                                    date = Date(),
                                    rssi = -56,
                                    txPower = -21,
                                    btId = "myId"
                                )
                            )
                        )
                    }
                }

                insert.await()
                aggregator.stop()
            }

            assertTrue(result.isSuccess)
        }
    }
}
