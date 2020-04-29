package org.immuni.android.networking

import org.immuni.android.networking.api.*
import org.immuni.android.networking.api.model.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response

class NetworkingRepositoryTest {

    @MockK(relaxed = true)
    lateinit var store: NetworkingStore

    @MockK(relaxed = true)
    lateinit var config: NetworkingConfiguration

    @MockK(relaxed = true)
    lateinit var networkingService: NetworkingService

    @MockK(relaxed = true)
    lateinit var settingsChannel: ConflatedBroadcastChannel<MyCustomSetting>

    @MockK(relaxed = true)
    lateinit var meChannel: ConflatedBroadcastChannel<NetworkingMe>

    @JsonClass(generateAdapter = true)
    class MyCustomSetting(
        @field:Json(name = "my_custom_field") val myCustomField: String? = null
    ): NetworkingSettings()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery { networkingService.me() } returns Response.success(200, "{\"device_id\":\"525\"}".toResponseBody())
        coEvery { networkingService.settings() } returns Response.success(
            200,
            "{\"min_build_version\":25,\"__privacy_notice_version__\":\"3\",\"__tos_version__\":\"4\",\"my_custom_field\":\"customValue\"}".toResponseBody())
    }

    @Test
    fun `settings populate base settings fields`() = runBlocking {
        val response = NetworkingRepository(
            networkingService,
            store,
            MyCustomSetting::class,
            NetworkingMe::class,
            settingsChannel,
            meChannel
        ).fetchSettings()
        assertEquals(25, response.body()?.minBuildVersion)
        assertEquals("4", response.body()?.tosVersion)
        assertEquals("3", response.body()?.privacyVersion)
    }

    @Test
    fun `settings populate custom settings field`() = runBlocking {
        val response = NetworkingRepository(
            networkingService,
            store,
            MyCustomSetting::class,
            NetworkingMe::class,
            settingsChannel,
            meChannel
        ).fetchSettings()

        // custom ones
        assertEquals("customValue", response.body()?.myCustomField)
    }

    @Test
    fun `settings are stored`() = runBlocking {
        val response = NetworkingRepository(
            networkingService,
            store,
            MyCustomSetting::class,
            NetworkingMe::class,
            settingsChannel,
            meChannel
        ).fetchSettings()

        verify { store.saveSettings(any<String>()) }
    }

    @Test
    fun `settings are sent to the broadcast channel`() = runBlocking {
        val response = NetworkingRepository(
            networkingService,
            store,
            MyCustomSetting::class,
            NetworkingMe::class,
            settingsChannel,
            meChannel
        ).fetchSettings()

        coVerify { settingsChannel.send(any<MyCustomSetting>()) }
    }

    @Test
    fun `me are stored`() = runBlocking {
        val response = NetworkingRepository(
            networkingService,
            store,
            MyCustomSetting::class,
            NetworkingMe::class,
            settingsChannel,
            meChannel
        ).fetchMe()

        verify { store.saveMe(any()) }
    }

    @Test
    fun `me are sent to the broadcast channel`() = runBlocking {
        val response = NetworkingRepository(
            networkingService,
            store,
            MyCustomSetting::class,
            NetworkingMe::class,
            settingsChannel,
            meChannel
        ).fetchMe()

        coVerify { meChannel.send(any()) }
    }
}
