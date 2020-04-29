package org.immuni.android.analytics

import org.immuni.android.analytics.api.PicoService
import org.immuni.android.analytics.api.model.PicoEventResponse
import org.immuni.android.analytics.model.PicoEvent
import io.mockk.*
import io.mockk.impl.annotations.MockK
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Response

@RunWith(JUnit4::class)
class PicoDispatcherTest {

    @MockK(relaxed = true)
    lateinit var api: PicoService

    @MockK(relaxed = true)
    lateinit var event: PicoEvent

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
    }

    @Test
    fun `dispatcher updates delta and latest_event_timestamp when call succeed`() = runBlocking {

        coEvery { api.event(any()) } returns Response.success(PicoEventResponse(
            delta = 1234,
            last_event_timestamp = 123456.0
        ))

        val dispatcher = PicoDispatcher(api)

        dispatcher.dispatchEvents(listOf(event))

        coVerify { api.event(any()) }

        assertEquals(1234, dispatcher.delta)
        assertEquals(123456.0, dispatcher.lastEventTimestamp, 0.0)
    }

    @Test
    fun `dispatcher doesn't updates delta and latest_event_timestamp when call fail`() = runBlocking {

        coEvery { api.event(any()) } returns Response.error(500, "{}".toResponseBody())

        val dispatcher = PicoDispatcher(api).apply {
            delta = 123
            lastEventTimestamp = 12.3
        }

        dispatcher.dispatchEvents(listOf(event))

        coVerify { api.event(any()) }

        assertEquals(123, dispatcher.delta)
        assertEquals(12.3, dispatcher.lastEventTimestamp, 0.0)
    }
}
