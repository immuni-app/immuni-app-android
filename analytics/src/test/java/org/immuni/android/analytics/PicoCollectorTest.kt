package org.immuni.android.analytics

import org.immuni.android.ids.IdsManager
import org.immuni.android.analytics.api.model.PicoEventResponse
import org.immuni.android.analytics.model.PicoEvent
import org.immuni.android.analytics.model.PicoUser
import org.immuni.android.analytics.userconsent.UserConsent
import org.immuni.android.analytics.userconsent.UserConsentLevel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Response

@RunWith(JUnit4::class)
class PicoCollectorTest {

    @MockK(relaxed = true)
    lateinit var store: PicoStore

    @MockK(relaxed = true)
    lateinit var picoUser: PicoUser

    @MockK(relaxed = true)
    lateinit var dispatcher: PicoDispatcher

    @MockK(relaxed = true)
    lateinit var config: PicoConfiguration

    @MockK(relaxed = true)
    lateinit var mockIds: IdsManager

    @MockK(relaxed = true)
    lateinit var event: PicoEvent

    @MockK(relaxed = true)
    internal lateinit var userConsent: UserConsent

    private lateinit var flow: PicoFlow
    private lateinit var collector: PicoCollector

    lateinit var successResponse: Response<PicoEventResponse>
    lateinit var networkingFailure: Response<PicoEventResponse>
    lateinit var invalidFormatFailure: Response<PicoEventResponse>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks

        coEvery { store.nextEventsBatch() } returns listOf(event)
        every { config.idsManager() } returns mockIds

        every { event.user } returns picoUser
        every { userConsent.level } returns UserConsentLevel.ACCEPTED

        flow =
            PicoFlow(store, userConsent, REPEAT = 1)
        collector =
            PicoCollector(flow, dispatcher, store)

        successResponse = Response.success(
            PicoEventResponse(
                delta = 1234,
                last_event_timestamp = 123456.0
            )
        )

        networkingFailure = Response.error(403, "{}".toResponseBody())
        invalidFormatFailure = Response.error(422, "{}".toResponseBody())
    }

    @Test
    fun `collector update event's request_timestamp`() = runBlockingTest {

        coEvery { dispatcher.dispatchEvents(any()) } returns successResponse
        collector.start()
        verify { event.requestTimestamp = any() }
    }

    @Test
    fun `collector try to dispatch events to the server`() = runBlockingTest {

        coEvery { dispatcher.dispatchEvents(any()) } returns successResponse
        collector.start()
        coVerify { dispatcher.dispatchEvents(any()) }
    }

    @Test
    fun `collector delete events from the store if dispatch succeed`() = runBlockingTest {

        coEvery { dispatcher.dispatchEvents(any()) } returns successResponse
        collector.start()
        coVerify { dispatcher.dispatchEvents(any()) }
        coVerify { store.deleteEvents(any()) }
    }

    @Test
    fun `collector doesn't delete events from the store if dispatch didn't succeed for networking or server problems (403, 500)`() = runBlockingTest {

        coEvery { dispatcher.dispatchEvents(any()) } returns networkingFailure
        collector.start()

        coVerify { dispatcher.dispatchEvents(any()) }
        coVerify(exactly = 0) { store.deleteEvents(any())}
    }

    @Test
    fun `collector delete events from the store if the server tells they are malformed (400, 422)`() = runBlockingTest {

        coEvery { dispatcher.dispatchEvents(any()) } returns invalidFormatFailure
        collector.start()

        coVerify { dispatcher.dispatchEvents(any()) }
        coVerify(exactly = 1) { store.deleteEvents(any())}
    }
}
