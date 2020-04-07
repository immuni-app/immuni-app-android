package org.immuni.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.dao.BLEContactDao
import org.immuni.android.db.entity.BLEContactEntity
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
    @Throws(Exception::class)
    fun canAddBleContacts() {
        runBlocking {
            bleDao.insert(
                BLEContactEntity(
                    btId = "123"
                ),
                BLEContactEntity(
                    btId = "456"
                ))

            assertEquals(2, bleDao.getAllBtIdsCount())
            assertTrue(bleDao.getAllDistinctBtIds().containsAll(listOf("123", "456")))
        }
    }

    @Test
    @Throws(Exception::class)
    fun canRemoveBleContactsOlderThan() {
        runBlocking {

            val MAX_DAYS = 28

            val validDate = Calendar.getInstance().apply {
                add(Calendar.DATE, -26)
            }
            val removableDate = Calendar.getInstance().apply {
                add(Calendar.DATE, -28)
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
