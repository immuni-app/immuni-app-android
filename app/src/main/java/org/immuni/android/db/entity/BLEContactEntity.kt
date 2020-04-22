package org.immuni.android.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.*
import kotlin.math.ceil

@Entity(tableName = "ble_contact_table", primaryKeys = ["timestamp", "btId"])
data class BLEContactEntity(
    var btId: String,
    var timestamp: Date = Date(),
    @ColumnInfo(name="events", typeAffinity = ColumnInfo.BLOB)
    var events: ByteArray = ByteArray(0)
) {
    val enumeratedEvents: List<BLEEvent>
        get() = events.asIterable().chunked(3) { BLEEvent(byteArray = it.toByteArray()) }.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BLEContactEntity

        if (timestamp != other.timestamp) return false
        if (btId != other.btId) return false
        if (!events.contentEquals(other.events)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + btId.hashCode()
        result = 31 * result + events.contentHashCode()
        return result
    }
}

data class BLEEvent (
    val relativeTimestamp: Int,
    val txPower: Int,
    val rssi: Int
) {
    constructor(byteArray: ByteArray) : this(
        relativeTimestamp = byteArray[0].toUByte().toInt(),
        txPower = byteArray[1].toInt(),
        rssi = byteArray[2].toInt()
    )
    fun toByteArray(): ByteArray {
        val array = ByteArray(3)
        array[0] = relativeTimestamp.toUByte().toByte()
        array[1] = txPower.toByte()
        array[2] = rssi.toByte()

        return array
    }
}

// number of slot available in each record blob
const val SLOTS_PER_CONTACT_RECORD = 256
// Value used to discretize the timestamps
const val RELATIVE_TIMESTAMP_SECONDS = 5
// Compensate for potential timer ticks delays with respect to the real timestamp
const val RELATIVE_TIMESTAMP_TOLERANCE_MS = 500

// Returns a relativetimestamp of `self` with respect to the given reference date
fun dateToRelativeTimestamp(referenceDate: Date, now: Date = Date()) : Int {
    return (ceil((now.time + RELATIVE_TIMESTAMP_TOLERANCE_MS) / 1000.0) - (referenceDate.time / 1000)).toInt() / RELATIVE_TIMESTAMP_SECONDS
}

// Creates a timestamp given the relative timestamp and a reference date.
// Note that the relativetimestamp should be created using `relativeTimestamp`
fun relativeTimestampToDate(referenceDate: Date, relativeTimestamp: Int) : Date {
    val ms = (RELATIVE_TIMESTAMP_SECONDS * relativeTimestamp * 1000) + referenceDate.time
    return Date(ms)
}
