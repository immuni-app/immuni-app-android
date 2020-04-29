package org.immuni.android.ids

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class IdsNonBackupStorageInstrumentedTest {

    @Test
    fun testIdSerializationSavingAndRestore() {

        val storage = IdsNonBackupStorageImpl(
            ApplicationProvider.getApplicationContext()
        ) as IdsStorage
        val saved = Ids.Id.Internal(
            Ids.InternalId.NON_BACKUP_PERSISTENT_ID, "123456", Ids.CreationType.justGenerated)
        storage.save(saved)

        val loaded = storage.get(Ids.InternalId.NON_BACKUP_PERSISTENT_ID)
        assertEquals(saved.copy(creation = Ids.CreationType.readFromFile), loaded)
    }
}

