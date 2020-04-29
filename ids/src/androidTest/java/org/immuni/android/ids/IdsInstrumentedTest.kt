package org.immuni.android.ids

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class IdsInstrumentedTest {

    val appCustomIdProvider = object :
        CustomIdProvider {
        override val ids: Set<Ids.Id>
            get() = setOf()
    }

    @Test
    fun testIdSerializationSavingAndRestore() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val storage = IdsStorageImpl(
            ctx,
            false
        ) as IdsStorage
        val saved = Ids.Id.Internal(
            Ids.InternalId.BACKUP_PERSISTENT_ID, "123456", Ids.CreationType.justGenerated)
        storage.save(saved)

        val loaded = storage.get(Ids.InternalId.BACKUP_PERSISTENT_ID)
        assertEquals(saved.copy(creation = Ids.CreationType.readFromFile), loaded)
    }

    @Test
    fun testJustGeneratedIdHasJustGeneratedCreationType() {
        val ctx: Context = ApplicationProvider.getApplicationContext()

        // clear storage
        IdsStorageImpl(ctx, false).apply {
            clear()
        }

        val manager: IdsManager = Ids.Manager(ctx, appCustomIdProvider = appCustomIdProvider, encryptIds = false)

        assertEquals(manager.backupPersistentId.creation, Ids.CreationType.justGenerated)
    }

    @Test
    fun testLoadedIdHasReadFromFileCreationType() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val storage = IdsStorageImpl(ctx, false)
        // clear storage
        storage.apply {
            clear()
        }

        // add id in storage
        storage.apply {
            val id = Ids.Id.Internal(
                Ids.InternalId.BACKUP_PERSISTENT_ID,
                "123", Ids.CreationType.justGenerated)
            save(id)
        }

        val manager: IdsManager = Ids.Manager(ctx, appCustomIdProvider = appCustomIdProvider, encryptIds = false)

        assertEquals(manager.backupPersistentId.creation, Ids.CreationType.readFromFile)
    }

    @Test
    fun testConciergeInitCreateACorrectInstance() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val manager: IdsManager = Ids.Manager(ctx, appCustomIdProvider = appCustomIdProvider, encryptIds = false)

        assertTrue(manager is IdsManagerImpl)
    }

    @Test
    fun testAcceptACustomStorageAndProvider() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val customNonBackupStorage =
            IdsStorageImpl(ctx, false)
        val customProvider = IdsProviderImpl(ctx)

        val manager: IdsManager = Ids.Manager(ctx,
            nonBackupStorage = customNonBackupStorage,
            provider = customProvider,
            appCustomIdProvider = appCustomIdProvider,
            encryptIds = false)

        assertSame(customNonBackupStorage, manager.nonBackupStorage)
        assertSame(customProvider, manager.provider)
    }
}

