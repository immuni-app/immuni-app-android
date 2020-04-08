package com.bendingspoons.concierge

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ConciergeStorageInstrumentedTest {

    @Test
    fun testIdSerializationSavingAndRestore() {

        val storage = ConciergeStorageImpl(ApplicationProvider.getApplicationContext()) as ConciergeStorage
        val saved = Concierge.Id.Internal(Concierge.InternalId.AAID, "123456", Concierge.CreationType.justGenerated)
        storage.save(saved)

        val loaded = storage.get(Concierge.InternalId.AAID)
        assertEquals(saved.copy(creation = Concierge.CreationType.readFromFile), loaded)
    }
}

