package org.immuni.android.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.immuni.android.db.dao.BLEContactDao
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.RELATIVE_TIMESTAMP_SECONDS
import org.immuni.android.db.entity.RELATIVE_TIMESTAMP_TOLERANCE_MS
import org.immuni.android.db.entity.relativeTimestampToDate
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class RoomBLEContactsTest {

    private lateinit var bleDao: BLEContactDao
    private lateinit var db: ImmuniDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ImmuniDatabase::class.java).build()
        bleDao = db.bleContactDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun canAddBleContacts() {
        runBlocking {
            val currentDate = Date()
            val fiveSecondsMore = Date(currentDate.time + 4800)
            db.addContact(btId = "123", txPower = -60, rssi = 23, date = currentDate)
            db.addContact(btId = "123", txPower = -58, rssi = 22, date = fiveSecondsMore)

            val contacts = bleDao.getAll()
            assertEquals(1, contacts.size)
            assertEquals("123", contacts[0].btId)
            val entries = contacts[0].enumeratedEvents
            assertEquals(2, entries.size)
            assertEquals(-60, entries[0].txPower)
            assertEquals(23, entries[0].rssi)
            assertEquals(currentDate, relativeTimestampToDate(currentDate, entries[0].relativeTimestamp))
            assertEquals(-58, entries[1].txPower)
            assertEquals(22, entries[1].rssi)
            assertEquals(
                fiveSecondsMore.time / 1000.0,
                relativeTimestampToDate(currentDate, entries[1].relativeTimestamp).time / 1000.0,
                RELATIVE_TIMESTAMP_TOLERANCE_MS / 1000.0
            )

        }
    }

    @Test
    fun canAddBleContacts2() {
        runBlocking {
            val currentDate = Date()
            val twentyMinutesMore = Date(currentDate.time + RELATIVE_TIMESTAMP_SECONDS * 256 * 1000)
            db.addContact(btId = "123", txPower = -60, rssi = 23, date = currentDate)
            db.addContact(btId = "123", txPower = -58, rssi = 22, date = twentyMinutesMore)

            val contacts = bleDao.getAll()
            assertEquals(2, contacts.size)
            assertEquals("123", contacts[0].btId)
            val entries0 = contacts[0].enumeratedEvents
            assertEquals(1, entries0.size)
            val record0 = entries0[0]
            assertEquals(0, record0.relativeTimestamp)
            assertEquals(-60, record0.txPower)
            assertEquals(23, record0.rssi)
            assertEquals(
                currentDate.time / 1000.0,
                relativeTimestampToDate(currentDate, record0.relativeTimestamp).time / 1000.0,
                RELATIVE_TIMESTAMP_TOLERANCE_MS / 1000.0
            )
            val entries1 = contacts[1].enumeratedEvents
            assertEquals(1, entries1.size)
            val record1 = entries1[0]
            assertEquals(0, record1.relativeTimestamp)
            assertEquals(-58, record1.txPower)
            assertEquals(22, record1.rssi)
            assertEquals(
                twentyMinutesMore.time / 1000.0,
                relativeTimestampToDate(twentyMinutesMore, record1.relativeTimestamp).time / 1000.0,
                RELATIVE_TIMESTAMP_TOLERANCE_MS / 1000.0
            )

        }
    }

    @Test
    @Throws(Exception::class)
    fun canRemoveBleContactsOlderThan() {
        runBlocking {

            val MAX_DAYS = 28

            val validDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -26)
            }
            val removableDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -28)
            }

            bleDao.insert(
                BLEContactEntity(
                    btId = "123"
                ),
                BLEContactEntity(
                    btId = "456",
                    timestamp = validDate.time
                ),
                BLEContactEntity(
                    btId = "789",
                    timestamp = removableDate.time
                ))

            assertEquals(3, bleDao.getAllBtIdsCount())
            assertTrue(bleDao.getAllDistinctBtIds().containsAll(listOf("123", "456")))

            // remove the data
            bleDao.removeOlderThan(
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DATE, -MAX_DAYS)
                }.timeInMillis)
            assertEquals(2, bleDao.getAllBtIdsCount())
            assertFalse(bleDao.getAllDistinctBtIds().contains("789"))
        }
    }
}
