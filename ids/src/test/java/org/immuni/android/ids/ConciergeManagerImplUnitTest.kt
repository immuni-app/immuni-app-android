package org.immuni.android.ids

import android.content.Context
import io.mockk.*
import org.immuni.android.ids.*
import org.junit.Test

import org.junit.Assert.*
import java.lang.Exception

class ConciergeManagerImplUnitTest {

    var customIds = setOf(Concierge.Id.Custom("custom_name", "value"))
    val appCustomIdProvider: ConciergeCustomIdProvider = object :
        ConciergeCustomIdProvider {
        override val ids: Set<Concierge.Id>
            get() = customIds
    }

    @Test
    fun `test custom ids cannot contain internal ids`() {
        val mockContext = mockk<Context>(relaxed = true)

        try {
            val manager: ConciergeManager = Concierge.Manager(
                mockContext,
                appCustomIdProvider = appCustomIdProvider,
                encryptIds = false)
            fail("Custom IDs contains Internal IDs.")
        } catch (e: Exception) { }

        customIds = setOf(
            Concierge.Id.Internal(
                Concierge.InternalId.BACKUP_PERSISTENT_ID,"value", Concierge.CreationType.justGenerated))

        try {
            val manager: ConciergeManager = Concierge.Manager(
                mockContext,
                appCustomIdProvider = appCustomIdProvider,
                encryptIds = false)
            fail("Custom IDs contains Internal IDs.")
        } catch (e: Exception) { }
    }

    @Test
    fun `test customId returns the correct id`() {
        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)

        customIds = setOf(Concierge.Id.Custom("custom_name", "value"))
        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        assertEquals("custom_name", manager.customId("custom_name")?.name)
    }

    @Test
    fun `test internalId returns the correct id`() {
        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        manager.backupPersistentId = Concierge.Id.Internal(
            Concierge.InternalId.BACKUP_PERSISTENT_ID, "", Concierge.CreationType.justGenerated)
        manager.nonBackupPersistentId = Concierge.Id.Internal(
            Concierge.InternalId.NON_BACKUP_PERSISTENT_ID, "", Concierge.CreationType.justGenerated)

        assertEquals("backup_persistent_id", manager.internalId(Concierge.InternalId.BACKUP_PERSISTENT_ID)?.name)
        assertEquals("non_backup_persistent_id", manager.internalId(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID)?.name)
    }

    @Test
    fun `test storage values are read during init`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        verify { storage.get(Concierge.InternalId.BACKUP_PERSISTENT_ID) }
        verify { storage.get(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID) }
    }

    @Test
    fun `test storage values are written during init`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)

        every { storage.get(any()) } returns null
        coEvery { provider.provideBackupPersistentId() } returns Concierge.Id.Custom("test","test")
        coEvery { provider.provideNonBackupPersistentId() } returns Concierge.Id.Custom("test","test")

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        val slot = slot<Concierge.Id>()
        verify(atLeast = 2) { storage.save(id = capture(slot)) }
        assertEquals(slot.captured.name, "test")
    }

    @Test
    fun `test storage values are not written during init if they don't exist`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val nonBackupStorage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)

        every { storage.get(any()) } returns null
        every { nonBackupStorage.get(any()) } returns null
        coEvery { provider.provideBackupPersistentId() } returns Concierge.Id.Custom("test","test")
        coEvery { provider.provideNonBackupPersistentId() } returns Concierge.Id.Custom("test","test")

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                nonBackupStorage,
                provider,
                appCustomIdProvider
            )

        verify(exactly = 1) { storage.save(any()) }
        verify(exactly = 1) { nonBackupStorage.save(any()) }
    }

    @Test
    fun `test provider is called if storage has no values during init`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)

        every { storage.get(any()) } returns null

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        coEvery { provider.provideNonBackupPersistentId() }
        coEvery { provider.provideBackupPersistentId()  }
    }

    @Test
    fun `test provider is not called if storage has values during init`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)
        val id = mockk<Concierge.Id>()

        every { storage.get(any()) } returns id

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        coVerify(exactly = 0) {provider.provideNonBackupPersistentId() }
        coVerify(exactly = 0) {provider.provideBackupPersistentId() }
    }


    @Test
    fun `test backupPersistentId is set during init even if already stored`() {
        
        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)
        val id = Concierge.Id.Internal(Concierge.InternalId.BACKUP_PERSISTENT_ID, "my_backupPersistentId_id", Concierge.CreationType.justGenerated)

        every { storage.get(Concierge.InternalId.BACKUP_PERSISTENT_ID) } returns id

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        assertEquals(manager.backupPersistentId, id)
    }

    @Test
    fun `test nonBackupPersistentId is set during init even if already stored`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)
        val id = Concierge.Id.Internal(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID, "my_backupPersistentId_id", Concierge.CreationType.justGenerated)

        every { storage.get(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID) } returns id

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        assertEquals(manager.nonBackupPersistentId, id)
    }

    @Test
    fun `test backupPersistentId is set during init when it is not stored yet`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)
        val id = Concierge.Id.Internal(Concierge.InternalId.BACKUP_PERSISTENT_ID, "my_backupPersistentId_id", Concierge.CreationType.justGenerated)

        every { storage.get(Concierge.InternalId.BACKUP_PERSISTENT_ID) } returns null
        coEvery { provider.provideBackupPersistentId() } returns id

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        assertEquals(manager.backupPersistentId, id)
    }

    @Test
    fun `test nonBackupPersistentId is set during init when it is not stored yet`() {

        val storage = mockk<ConciergeStorage>(relaxed = true)
        val provider = mockk<ConciergeProvider>(relaxed = true)
        val id = Concierge.Id.Internal(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID, "my_backupPersistentId_id", Concierge.CreationType.justGenerated)

        every { storage.get(Concierge.InternalId.NON_BACKUP_PERSISTENT_ID) } returns null
        coEvery { provider.provideNonBackupPersistentId() } returns id

        val manager: ConciergeManager =
            ConciergeManagerImpl(
                storage,
                storage,
                provider,
                appCustomIdProvider
            )

        assertEquals(manager.nonBackupPersistentId, id)
    }
}
