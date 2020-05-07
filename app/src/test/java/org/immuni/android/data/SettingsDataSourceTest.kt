package org.immuni.android.data

import android.content.Context
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.immuni.android.api.AppConfigurationService
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.junit.Assert.assertNull
import org.junit.Test

import org.junit.Before
import kotlin.test.assertEquals

class SettingsDataSourceTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var store: SettingsStore

    @MockK(relaxed = true)
    lateinit var appConfigurationService: AppConfigurationService

    @MockK(relaxed = true)
    lateinit var lifecycle: AppLifecycleObserver

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

            val dataSource =
                SettingsDataSource(appConfigurationService, store, lifecycle)
            assertEquals(settings, dataSource.latestSettings())
        }

    @Test
    fun `settings channel is empty when settings are not present at startup`() = runBlockingTest {

        coEvery { store.loadSettings() } returns null

        val dataSource = SettingsDataSource(appConfigurationService, store, lifecycle)
        assertNull(dataSource.latestSettings())
    }

    @Test
    fun `settings flow contains settings at startup if settins are present`() = runBlocking {

        coEvery { store.loadSettings() } returns settings

        val dataSource = SettingsDataSource(appConfigurationService, store, lifecycle)
        val flow = dataSource.settingsFlow()

        var counter = 0
        async {
            flow.collect {
                counter++
                dataSource.closeSettingsChannel()
            }
        }.await()

        assertEquals(1, counter)
        Unit
    }

    @Test
    fun `settings flow emits settings`() = runBlocking {

        coEvery { store.loadSettings() } returns null

        val dataSource = SettingsDataSource(appConfigurationService, store, lifecycle)
        val flow = dataSource.settingsFlow()

        var counter = 0
        async {
            flow.collect {
                counter++
            }
        }

        async {
            for (i in 0 until 5) {
                dataSource.onSettingsUpdate(settings)
                delay(100)
            }
            assertEquals(5, counter)
            dataSource.closeSettingsChannel()
        }
        Unit
    }

    @Test
    fun `settings are stored when after fetching`() = runBlocking {
        val dataSource = SettingsDataSource(appConfigurationService, store, lifecycle)
        dataSource.onSettingsUpdate(settings)
        verify { store.saveSettings(settings) }
    }
}
