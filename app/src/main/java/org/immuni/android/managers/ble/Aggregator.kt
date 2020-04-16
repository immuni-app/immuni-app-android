package org.immuni.android.managers.ble

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.math.pow
import kotlin.math.roundToInt

class Aggregator: KoinComponent {

    private val database: ImmuniDatabase by inject()

    private val TIME_WINDOW = 10 * 1000
    private val proximityEvents = mutableListOf<BLEContactEntity>()

    fun addProximityEvents(events: List<BLEContactEntity>) {

        log("Raw scan: ${events.map { "${it.btId} - ${it.rssi}" }.joinToString(", ")}")

        proximityEvents.addAll(events)

        if(proximityEvents.isEmpty()) return

        val firstTs = proximityEvents.first().timestamp
        val lastTs = proximityEvents.last().timestamp

        if(lastTs.time - firstTs.time > TIME_WINDOW) {
            store(aggregate())
            clear()
        }
    }

    private fun aggregate(): Collection<BLEContactEntity> {
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

    private fun store(events: Collection<BLEContactEntity>) {
        GlobalScope.launch {
            database.bleContactDao().insert(*events.toTypedArray())
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
    lateinit var contact: BLEContactEntity

    fun newAverage(newContact: BLEContactEntity): RssiRollingAverage {
        val newAverage = RssiRollingAverage(
            countSoFar + 1,
            (averageRssi * countSoFar + newContact.rssi) / (countSoFar + 1).toDouble()
        )
        newAverage.contact = newContact.copy(rssi = newAverage.averageRssi.roundToInt())
        return newAverage
    }
}