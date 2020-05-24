/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.extensions.utils.defaultMoshi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class KVStorageTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var sharedPrefs: SharedPreferences

    private val cache = mutableMapOf<KVStorage.Key<*>, Any>()

    private lateinit var kvStorage: KVStorage

    private val myString = KVStorage.Key<String>("myString")
    private val myInt = KVStorage.Key<Int>("myInt")
    private val myLong = KVStorage.Key<Long>("myLong")
    private val myFloat = KVStorage.Key<Float>("myFloat")
    private val myBool = KVStorage.Key<Boolean>("myBool")
    private val myObj = KVStorage.Key<FakeObject>("myObj")

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        cache.clear()
        kvStorage = KVStorage(
            name = "name",
            context = context,
            cacheInMemory = true,
            encrypted = false,
            moshi = defaultMoshi,
            _sharedPrefs = sharedPrefs,
            _cache = cache
        )
    }

    @Test
    fun `kvStorage saves and restores primitive types from cache`() {
        kvStorage[myInt] = 1234
        assertEquals(1234, kvStorage[myInt])

        kvStorage[KVStorage.Key("myLong")] = 1234L
        assertEquals(1234L, kvStorage[myLong])

        kvStorage[KVStorage.Key("myFloat")] = 1234.5f
        assertEquals(1234.5f, kvStorage[myFloat])

        kvStorage[KVStorage.Key("myBool")] = true
        assertEquals(true, kvStorage[myBool])

        kvStorage[myString] = "abcdefgh"
        assertEquals("abcdefgh", kvStorage[myString])

        assertEquals(5, cache.size)
    }

    @Test
    fun `kvStorage saves and restores complex objects from cache`() {
        kvStorage[myObj] = FakeObject("mario", "rossi")
        assertEquals(FakeObject("mario", "rossi"), kvStorage[myObj])
    }

    @Test
    fun `kvStorage loads from sharedPrefs if value is not in cache`() {

        every { sharedPrefs.getString("myString", any()) } returns "myValue"
        every { sharedPrefs.contains("myString") } returns true

        cache.clear()
        assertEquals("myValue", kvStorage[myString])
    }

    @Test
    fun `kvStorage saves value into cache`() {
        kvStorage[myString] = "abcdefgh"
        assertEquals("abcdefgh", cache[myString])
    }

    @Test
    fun `kvStorage cache is invalidated after saving another value`() {
        kvStorage[myString] = "abcdefgh"
        assertEquals("abcdefgh", cache[myString])
        kvStorage[myString] = "ilmopqrst"
        assertEquals("ilmopqrst", cache[myString])
    }

    @Test
    fun `KVStorage exposes data as StateFlow of optional values`() {
        val flow = kvStorage.stateFlow(myString)
        assertEquals(null, flow.value)

        kvStorage[myString] = "abcdefgh"
        assertEquals("abcdefgh", flow.value)
        assertTrue(kvStorage.contains(myString))

        kvStorage[myString] = "xyz"
        assertEquals("xyz", flow.value)

        kvStorage.delete(myString)
        assertEquals(null, flow.value)
        assertFalse(kvStorage.contains(myString))
    }

    @Test
    fun `KVStorage exposes data as StateFlow of nonoptional values when a defaultValue is given`() {
        val flow = kvStorage.stateFlow(myString, "default")
        assertEquals("default", flow.value)

        kvStorage[myString] = "abcdefgh"
        assertEquals("abcdefgh", flow.value)
        assertTrue(kvStorage.contains(myString))

        kvStorage[myString] = "xyz"
        assertEquals("xyz", flow.value)

        kvStorage.delete(myString)
        assertEquals("default", flow.value)
        assertFalse(kvStorage.contains(myString))
    }
}

private data class FakeObject(
    val name: String,
    val surname: String
)
