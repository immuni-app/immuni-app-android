package com.bendingspoons.oracle

import com.bendingspoons.oracle.api.*
import com.bendingspoons.oracle.api.model.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response

class OracleRepositoryTest {

    @MockK(relaxed = true)
    lateinit var store: OracleStore

    @MockK(relaxed = true)
    lateinit var config: OracleConfiguration

    @MockK(relaxed = true)
    lateinit var oracleService: OracleService

    @MockK(relaxed = true)
    lateinit var settingsChannel: ConflatedBroadcastChannel<MyCustomSetting>

    @MockK(relaxed = true)
    lateinit var meChannel: ConflatedBroadcastChannel<OracleMe>

    @JsonClass(generateAdapter = true)
    class MyCustomSetting(
        @field:Json(name = "my_custom_field") val myCustomField: String? = null
    ): OracleSettings()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery { oracleService.me() } returns Response.success(200, "{\"is_subscribed\":true}".toResponseBody())
        coEvery { oracleService.settings() } returns Response.success(200, "{\"skip_paywall\":true,\"my_custom_field\":\"customValue\", \"__privacy_notice_url__\":\"myPp\", \"__tos_url__\":\"myTos\"}".toResponseBody())
    }

    @Test
    fun `settings populate base settings fields`() = runBlocking {
        val response = OracleRepository(oracleService, store, MyCustomSetting::class, OracleMe::class, settingsChannel, meChannel).fetchSettings()
        assertEquals("myTos", response.body()?.tosUrl)
        assertEquals("myPp", response.body()?.privacyUrl)
        assertEquals(true, response.body()?.skipPaywall)
    }

    @Test
    fun `settings populate custom settings field`() = runBlocking {
        val response = OracleRepository(oracleService, store, MyCustomSetting::class, OracleMe::class, settingsChannel, meChannel).fetchSettings()

        // custom ones
        assertEquals("customValue", response.body()?.myCustomField)
    }

    @Test
    fun `settings are stored`() = runBlocking {
        val response = OracleRepository(oracleService, store, MyCustomSetting::class, OracleMe::class, settingsChannel, meChannel).fetchSettings()

        verify { store.saveSettings(any<String>()) }
    }

    @Test
    fun `settings are sent to the broadcast channel`() = runBlocking {
        val response = OracleRepository(oracleService, store, MyCustomSetting::class, OracleMe::class, settingsChannel, meChannel).fetchSettings()

        coVerify { settingsChannel.send(any<MyCustomSetting>()) }
    }

    @Test
    fun `me are stored`() = runBlocking {
        val response = OracleRepository(oracleService, store, MyCustomSetting::class, OracleMe::class, settingsChannel, meChannel).fetchMe()

        verify { store.saveMe(any()) }
    }

    @Test
    fun `me are sent to the broadcast channel`() = runBlocking {
        val response = OracleRepository(oracleService, store, MyCustomSetting::class, OracleMe::class, settingsChannel, meChannel).fetchMe()

        coVerify { meChannel.send(any()) }
    }
}
