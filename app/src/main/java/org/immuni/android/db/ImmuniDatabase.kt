package org.immuni.android.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.immuni.android.db.converter.DateConverter
import org.immuni.android.db.converter.GenderConverter
import org.immuni.android.db.dao.BLEContactDao
import org.immuni.android.db.dao.HealthProfileDao
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.BLEEvent
import org.immuni.android.db.entity.HealthProfileEntity
import org.immuni.android.db.entity.dateToRelativeTimestamp
import org.immuni.android.util.log
import java.io.File
import java.util.*


const val DATABASE_VERSION = 8
const val DATABASE_NAME = "immuni_database"

@Database(
    entities = [
        BLEContactEntity::class,
        HealthProfileEntity::class
    ],
    version = DATABASE_VERSION
)
@TypeConverters(
    DateConverter::class,
    GenderConverter::class
)
abstract class ImmuniDatabase : RoomDatabase() {
    abstract fun bleContactDao(): BLEContactDao
    abstract fun healthProfileDao(): HealthProfileDao
    suspend fun addContact(btId: String, txPower: Int, rssi: Int, date: Date) {
        var entry = bleContactDao().getLatestByBtId(btId)
        if (entry == null) {
            entry = BLEContactEntity(btId = btId, timestamp = date)
        } else {
            val relativeTimestamp = dateToRelativeTimestamp(referenceDate = entry.timestamp, now = date)
            if (relativeTimestamp > 255) {
                log("creating a new entry because relativeTimestamp is: $relativeTimestamp")
                entry = BLEContactEntity(btId = btId, timestamp = date)
            }
        }

        entry.events += BLEEvent(
            relativeTimestamp = dateToRelativeTimestamp(referenceDate = entry.timestamp, now = date),
            txPower = txPower,
            rssi = rssi
        ).toByteArray()

        bleContactDao().insert(entry)
    }

    companion object {
        fun databaseSize(context: Context): Long {
            val file: File = context.getDatabasePath(DATABASE_NAME)
            return file.length()
        }
    }
}


