package com.bendingspoons.concierge

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ConciergeNonBackupStorageInstrumentedTest {

    @Test
    fun testIdSerializationSavingAndRestore() {

        val storage = ConciergeNonBackupStorageImpl(ApplicationProvider.getApplicationContext()) as ConciergeStorage
        val saved = Concierge.Id.Internal(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID, "123456", Concierge.CreationType.justGenerated)
        storage.save(saved)

        val loaded = storage.get(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID)
        assertEquals(saved.copy(creation = Concierge.CreationType.readFromFile), loaded)
    }
}

