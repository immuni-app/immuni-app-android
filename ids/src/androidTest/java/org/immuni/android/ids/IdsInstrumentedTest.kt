package org.immuni.android.ids

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class IdsInstrumentedTest {

    @Test
    fun testIdSerializationSavingAndRestore() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val storage = IdsStorageImpl(
            ctx,
            false
        ) as IdsStorage
        val saved = Ids.Id(
            ID_NAME, "123456", Ids.CreationType.justGenerated)
        storage.save(saved)

        val loaded = storage.get(ID_NAME)
        assertEquals(saved.copy(creation = Ids.CreationType.readFromFile), loaded)
    }

    @Test
    fun testJustGeneratedIdHasJustGeneratedCreationType() {
        val ctx: Context = ApplicationProvider.getApplicationContext()

        // clear storage
        IdsStorageImpl(ctx, false).apply {
            clear()
        }

        val manager: IdsManager = Ids(ctx, encryptIds = false).manager

        assertEquals(manager.id.creation, Ids.CreationType.justGenerated)
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
            val id = Ids.Id(
                ID_NAME,
                "123", Ids.CreationType.justGenerated)
            save(id)
        }

        val manager: IdsManager = Ids(ctx, encryptIds = false).manager

        assertEquals(manager.id.creation, Ids.CreationType.readFromFile)
    }
}

