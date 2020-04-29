package org.immuni.android.ids

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ConciergeInstrumentedTest {

    val appCustomIdProvider = object :
        ConciergeCustomIdProvider {
        override val ids: Set<Concierge.Id>
            get() = setOf()
    }

    @Test
    fun testIdSerializationSavingAndRestore() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val storage = ConciergeStorageImpl(
            ctx,
            false
        ) as ConciergeStorage
        val saved = Concierge.Id.Internal(
            Concierge.InternalId.BACKUP_PERSISTENT_ID, "123456", Concierge.CreationType.justGenerated)
        storage.save(saved)

        val loaded = storage.get(Concierge.InternalId.BACKUP_PERSISTENT_ID)
        assertEquals(saved.copy(creation = Concierge.CreationType.readFromFile), loaded)
    }

    @Test
    fun testJustGeneratedIdHasJustGeneratedCreationType() {
        val ctx: Context = ApplicationProvider.getApplicationContext()

        // clear storage
        ConciergeStorageImpl(ctx, false).apply {
            clear()
        }

        val manager: ConciergeManager = Concierge.Manager(ctx, appCustomIdProvider = appCustomIdProvider, encryptIds = false)

        assertEquals(manager.backupPersistentId.creation, Concierge.CreationType.justGenerated)
    }

    @Test
    fun testLoadedIdHasReadFromFileCreationType() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val storage = ConciergeStorageImpl(ctx, false)
        // clear storage
        storage.apply {
            clear()
        }

        // add id in storage
        storage.apply {
            val id = Concierge.Id.Internal(
                Concierge.InternalId.BACKUP_PERSISTENT_ID,
                "123", Concierge.CreationType.justGenerated)
            save(id)
        }

        val manager: ConciergeManager = Concierge.Manager(ctx, appCustomIdProvider = appCustomIdProvider, encryptIds = false)

        assertEquals(manager.backupPersistentId.creation, Concierge.CreationType.readFromFile)
    }

    @Test
    fun testConciergeInitCreateACorrectInstance() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val manager: ConciergeManager = Concierge.Manager(ctx, appCustomIdProvider = appCustomIdProvider, encryptIds = false)

        assertTrue(manager is ConciergeManagerImpl)
    }

    @Test
    fun testAcceptACustomStorageAndProvider() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val customNonBackupStorage =
            ConciergeStorageImpl(ctx, false)
        val customProvider = ConciergeProviderImpl(ctx)

        val manager: ConciergeManager = Concierge.Manager(ctx,
            nonBackupStorage = customNonBackupStorage,
            provider = customProvider,
            appCustomIdProvider = appCustomIdProvider,
            encryptIds = false)

        assertSame(customNonBackupStorage, manager.nonBackupStorage)
        assertSame(customProvider, manager.provider)
    }
}

