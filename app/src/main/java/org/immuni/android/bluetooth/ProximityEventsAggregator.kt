package org.immuni.android.bluetooth

import org.immuni.android.networking.Networking
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.dao.addContact
import org.immuni.android.db.entity.SLOTS_PER_CONTACT_RECORD
import org.immuni.android.models.ProximityEvent
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import kotlin.math.pow
import kotlin.math.roundToInt

class ProximityEventsAggregator(
    val database: ImmuniDatabase,
    val networking: Networking<ImmuniSettings>,
    val TIME_WINDOW: Long
    ): KoinComponent {

    private val mutex = Mutex()
    private var timerJob: Job? = null

    private val proximityEvents = mutableListOf<ProximityEvent>()

    suspend fun start() = coroutineScope {
        timerJob = launch {
            repeat(Int.MAX_VALUE) {
                delay(TIME_WINDOW)
                tick()
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
    }

    fun addProximityEvents(events: List<ProximityEvent>) = runBlocking {
        log("Raw scan: ${events.map { "${it.btId} - ${it.rssi}" }.joinToString(", ")}")
        mutex.withLock {
            proximityEvents.addAll(events)
        }
    }

    private suspend fun tick() {
        log("Aggregator tick...")
        if (proximityEvents.isEmpty()) return
        mutex.withLock {
            store(aggregate())
            clear()
        }
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

    private suspend fun store(events: Collection<ProximityEvent>) {
        val slots = (networking.settings()?.bleSlotsPerContactRecord ?: SLOTS_PER_CONTACT_RECORD)
        events.forEach {
            database.bleContactDao().addContact(
                btId = it.btId,
                txPower = it.txPower,
                rssi = it.rssi,
                date = it.date,
                slots = slots
            )
        }
        database.rawDao().checkpoint()
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