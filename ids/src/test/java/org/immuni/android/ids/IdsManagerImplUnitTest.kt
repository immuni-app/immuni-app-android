package org.immuni.android.ids

import io.mockk.*
import org.junit.Test
import org.junit.Assert.*

class IdsManagerImplUnitTest {

    @Test
    fun `test storage values are read during init`() {

        val storage = mockk<IdsStorage>(relaxed = true)
        val provider = mockk<IdsProvider>(relaxed = true)

        val manager = IdsManagerImpl(
                storage,
                provider
            )

        verify { storage.get(ID_NAME) }
    }

    @Test
    fun `test storage values are written during init`() {

        val storage = mockk<IdsStorage>(relaxed = true)
        val provider = mockk<IdsProvider>(relaxed = true)

        every { storage.get(any()) } returns null
        coEvery { provider.provideId() } returns Ids.Id(ID_NAME,"test", creation = Ids.CreationType.justGenerated)

        val manager = IdsManagerImpl(
            storage,
            provider
        )

        val slot = slot<Ids.Id>()
        verify { storage.save(id = capture(slot)) }
        assertEquals(slot.captured.name, ID_NAME)
    }

    @Test
    fun `test provider is called if storage has no values during init`() {

        val storage = mockk<IdsStorage>(relaxed = true)
        val provider = mockk<IdsProvider>(relaxed = true)

        every { storage.get(any()) } returns null

        val manager = IdsManagerImpl(
            storage,
            provider
        )

        coEvery { provider.provideId() }
    }

    @Test
    fun `test provider is not called if storage has values during init`() {

        val storage = mockk<IdsStorage>(relaxed = true)
        val provider = mockk<IdsProvider>(relaxed = true)
        val id = mockk<Ids.Id>()

        every { storage.get(any()) } returns id

        val manager = IdsManagerImpl(
            storage,
            provider
        )

        coVerify(exactly = 0) {provider.provideId() }
    }
}
