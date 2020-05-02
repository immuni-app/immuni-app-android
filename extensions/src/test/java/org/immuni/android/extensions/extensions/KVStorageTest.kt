package org.immuni.android.extensions.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.Assert.assertEquals
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.extensions.utils.round
import org.junit.Before
import org.junit.Test

class KVStorageTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var sharedPrefs: SharedPreferences

    val cache = mutableMapOf<String, Any>()

    lateinit var kvStorage: KVStorage
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        cache.clear()
        kvStorage = KVStorage("name", context, true, false, sharedPrefs, cache)
    }

    @Test
    fun `kvStorage saves and restores primitive types from cache`() {
        kvStorage.save("myInt", 1234)
        assertEquals(1234, kvStorage.load("myInt")!!)

        kvStorage.save("myLong", 1234L)
        assertEquals(1234L, kvStorage.load("myLong")!!)

        kvStorage.save("myFloat", 1234.5f)
        assertEquals(1234.5f, kvStorage.load("myFloat")!!)

        kvStorage.save("myBool", true)
        assertEquals(true, kvStorage.load("myBool")!!)

        kvStorage.save("myString", "abcdefgh")
        assertEquals("abcdefgh", kvStorage.load("myString")!!)

        assertEquals(5, cache.size)
    }

    @Test
    fun `kvStorage saves and restores complex objects from cache`() {
        kvStorage.save("myObj", FakeObject("mario", "rossi"))
        assertEquals(FakeObject("mario", "rossi"), kvStorage.load("myObj")!!)
    }

    @Test
    fun `kvStorage loads from sharedPrefs if value is not in cache`() {

        every { sharedPrefs.getString("myString", any()) } returns "myValue"
        every { sharedPrefs.contains("myString") } returns true

        cache.clear()
        assertEquals("myValue", kvStorage.load("myString")!!)

    }

    @Test
    fun `kvStorage saves value into cache`() {
        kvStorage.save("myString", "abcdefgh")
        assertEquals("abcdefgh", cache["myString"])
    }

    @Test
    fun `kvStorage cache is invalidated after saving another value`() {
        kvStorage.save("myString", "abcdefgh")
        assertEquals("abcdefgh", cache["myString"])
        kvStorage.save("myString", "ilmopqrst")
        assertEquals("ilmopqrst", cache["myString"])
    }
}

private data class FakeObject(
    val name: String,
    val surname: String
)