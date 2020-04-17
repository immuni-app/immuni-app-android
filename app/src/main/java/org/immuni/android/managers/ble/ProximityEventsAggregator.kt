package org.immuni.android.managers.ble

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.models.ProximityEvent
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.concurrent.timer
import kotlin.math.pow
import kotlin.math.roundToInt

class ProximityEventsAggregator: KoinComponent {
    companion object {
        private const val TIME_WINDOW: Long = 10 * 1000
    }

    private val database: ImmuniDatabase by inject()

    private val timer = timer(name ="aggregator-timer", initialDelay = TIME_WINDOW, period = TIME_WINDOW) {
        tick()
    }
    private val proximityEvents = mutableListOf<ProximityEvent>()

    fun addProximityEvents(events: List<ProximityEvent>) {
        log("Raw scan: ${events.map { "${it.btId} - ${it.rssi}" }.joinToString(", ")}")
        synchronized(this) {
            proximityEvents.addAll(events)
        }
    }

    private fun tick() {
        if (proximityEvents.isEmpty()) return
        store(aggregate())
        clear()
    }

    private fun aggregate(): Collection<ProximityEvent> {
        // if in the same scan result we have the same ids, compute the average rssi
        val rssisGroupedById = proximityEvents.groupingBy { it.btId }
        val averagedRssisGroupedById =
            rssisGroupedById.fold(RssiRollingAverage()) { rollingAverage, contact ->
                rollingAverage.newAverage(contact)
            }

        val averagedRssiContactsGroupedById = averagedRssisGroupedById.mapValues {
            it.value.contact
        }

        averagedRssiContactsGroupedById.values.forEach {
            log("Aggregate scan: ${it.btId} - ${it.rssi} - distance: ${distance(it.rssi, it.txPower)} meters")
        }

        return averagedRssiContactsGroupedById.values
    }

    private fun clear() {
        proximityEvents.clear()
    }

    private fun store(events: Collection<ProximityEvent>) {
        GlobalScope.launch {
            events.forEach {
                database.addContact(
                    btId = it.btId,
                    txPower = it.txPower,
                    rssi = it.rssi,
                    date = it.date
                )
            }
        }
    }

    fun distance(rssi: Int, txPower: Int): Float {
        val p0 = -89f
        val gamma = 2f
        return 10.0.pow((p0 - rssi) / (10.0 * gamma)).toFloat()
    }
}

internal data class RssiRollingAverage(
    val countSoFar: Int = 0,
    val averageRssi: Double = 0.0
) {
    lateinit var contact: ProximityEvent

    fun newAverage(newContact: ProximityEvent): RssiRollingAverage {
        val newAverage = RssiRollingAverage(
            countSoFar + 1,
            (averageRssi * countSoFar + newContact.rssi) / (countSoFar + 1).toDouble()
        )
        newAverage.contact = newContact.copy(rssi = newAverage.averageRssi.roundToInt())
        return newAverage
    }
}