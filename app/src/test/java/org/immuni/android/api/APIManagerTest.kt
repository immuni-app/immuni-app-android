package org.immuni.android.api

import android.content.Context
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.wait
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.network.Network
import org.immuni.android.network.NetworkConfiguration
import org.junit.Assert.assertNull
import org.junit.Test

import org.junit.Before
import retrofit2.Response
import kotlin.test.assertEquals

class APIManagerTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var store: APIStore

    @MockK(relaxed = true)
    lateinit var repository: APIRepository

    @MockK(relaxed = true)
    lateinit var settings: ImmuniSettings

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `settings channel is initialized with value when settings are present at startup`() =
        runBlockingTest {

            coEvery { store.loadSettings() } returns settings

            val manager = APIManager(repository, store)
            assertEquals(settings, manager.latestSettings())
        }

    @Test
    fun `settings channel is empty when settings are not present at startup`() = runBlockingTest {

        coEvery { store.loadSettings() } returns null

        val manager = APIManager(repository, store)
        assertNull(manager.latestSettings())
    }

    @Test
    fun `settings flow contains settings at startup if settins are present`() = runBlocking {

        coEvery { store.loadSettings() } returns settings

        val manager = APIManager(repository, store)
        val flow = manager.settingsFlow()

        var counter = 0
        async {
            flow.collect {
                counter++
                manager.closeSettingsChannel()
            }
        }.await()

        assertEquals(1, counter)
        Unit
    }

    @Test
    fun `settings flow emits settings`() = runBlocking {

        coEvery { store.loadSettings() } returns null

        val manager = APIManager(repository, store)
        val flow = manager.settingsFlow()

        var counter = 0
        async {
            flow.collect {
                counter++
            }
        }

        async {
            for (i in 0 until 5) {
                manager.onSettingsUpdate(settings)
                delay(100)
            }
            assertEquals(5, counter)
            manager.closeSettingsChannel()
        }
        Unit
    }
}
