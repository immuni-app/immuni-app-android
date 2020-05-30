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

package it.ministerodellasalute.immuni.logic

import android.content.Context
import io.mockk.*
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.api.services.ConfigurationSettings
import it.ministerodellasalute.immuni.api.services.Language
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsNetworkRepository
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsStoreRepository
import kotlin.test.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class ConfigurationSettingsManagerTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    lateinit var manager: ConfigurationSettingsManager

    @MockK(relaxed = true)
    lateinit var storeRepository: ConfigurationSettingsStoreRepository

    @MockK(relaxed = true)
    lateinit var networkRepository: ConfigurationSettingsNetworkRepository

    @MockK(relaxed = true)
    lateinit var lifecycle: AppLifecycleObserver

    @MockK(relaxed = true)
    lateinit var settings: ConfigurationSettings

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        manager = ConfigurationSettingsManager(
            networkRepository = networkRepository,
            storeRepository = storeRepository,
            buildVersion = 1
        )
    }

    @Test
    fun `settings channel is initialized with value when settings are present at startup`() =
        runBlockingTest {
            coEvery { storeRepository.loadSettings() } returns settings
            manager = ConfigurationSettingsManager(
                networkRepository = networkRepository,
                storeRepository = storeRepository,
                buildVersion = 1
            )

            assertEquals(settings, manager.settings.value)
        }

    @Test
    fun `settings flow emits settings`() = runBlocking {

        val flow = manager.settings

        var counter = 0
        val deferred = async {
            flow.collect {
                counter++
            }
        }

        async {
            for (i in 1..5) {
                manager.onSettingsUpdate(settings.copy(termsOfUseUrls = mapOf(Language.EN.code to i.toString())))
                delay(100)
            }
            deferred.cancel()
            assertEquals(6, counter)
        }
        Unit
    }

    @Test
    fun `settings are stored when after fetching`() = runBlocking {
        manager.onSettingsUpdate(settings)
        verify { storeRepository.saveSettings(settings) }
    }
}
