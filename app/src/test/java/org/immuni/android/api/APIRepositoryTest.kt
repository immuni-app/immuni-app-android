package org.immuni.android.api

import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.network.Network
import org.immuni.android.network.NetworkConfiguration
import org.junit.Test

import org.junit.Before
import retrofit2.Response

class APIRepositoryTest {

    @MockK(relaxed = true)
    lateinit var store: APIStore

    @MockK(relaxed = true)
    lateinit var config: NetworkConfiguration

    @MockK(relaxed = true)
    lateinit var network: Network

    @MockK(relaxed = true)
    lateinit var apiService: API

    @MockK(relaxed = true)
    lateinit var apiListener: APIListener

    @MockK(relaxed = true)
    lateinit var settings: ImmuniSettings

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { network.createServiceAPI<API>(any()) } returns apiService
    }

    @Test
    fun `settings are stored when after fetching`() = runBlocking {
        coEvery { apiService.settings() } returns Response.success(200, settings)

        APIRepository(network, store).settings()

        verify { store.saveSettings(settings) }
    }

    @Test
    fun `settings are not stored when fetching fails`() = runBlocking {
        coEvery { apiService.settings() } returns Response.error(500, "".toResponseBody())

        APIRepository(network, store).settings()

        verify(exactly = 0) { store.saveSettings(settings) }
    }

    @Test
    fun `settings are delivered to API listener after fetching`() = runBlocking {
        coEvery { apiService.settings() } returns Response.success(200, settings)

        val repository = APIRepository(network, store)

        repository.addAPIListener(apiListener)
        repository.settings()

        coVerify { apiListener.onSettingsUpdate(settings) }
    }

    @Test
    fun `settings are not delivered to API listener when fetching fails`() = runBlocking {
        coEvery { apiService.settings() } returns Response.error(500, "".toResponseBody())

        val repository = APIRepository(network, store)

        repository.addAPIListener(apiListener)
        repository.settings()

        coVerify(exactly = 0) { apiListener.onSettingsUpdate(settings) }
    }
}
