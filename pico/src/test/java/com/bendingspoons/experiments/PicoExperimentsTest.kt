package com.bendingspoons.experiments

import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.pico.PicoEventManager
import com.bendingspoons.pico.experiments.ExperimentsSegmentReceivedManager
import com.bendingspoons.pico.experiments.ExperimentsStore
import com.bendingspoons.pico.model.ExperimentSegmentsReceived
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PicoExperimentsTest {

    @MockK(relaxed = true)
    lateinit var store: ExperimentsStore

    @MockK(relaxed = true)
    lateinit var oracle: Oracle<OracleSettings, OracleMe>

    @MockK(relaxed = true)
    lateinit var eventsManager: PicoEventManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
    }

    @Test
    fun `if there are new settings the event is sent`() = runBlockingTest {

        every { store.loadSegments() } returns mapOf() // old settings are empty
        val channel = ConflatedBroadcastChannel<OracleSettings>()
        every { oracle.settingsFlow() } returns channel.asFlow()

        val experimentsManager = ExperimentsSegmentReceivedManager(store, oracle.settingsFlow(), eventsManager)

        channel.send(
            OracleSettings(
                experimentsSegments = mapOf("segment_1" to 2) // a new segment added
            ))

        async {
            experimentsManager.start()
        }

        delay(100)
        channel.close()

        coVerify { eventsManager.trackEvent(any<ExperimentSegmentsReceived>()) }
    }

    @Test
    fun `if there are no new settings the event is not sent`() = runBlockingTest {

        every { store.loadSegments() } returns mapOf("segment_1" to 2) // old settings
        val channel = ConflatedBroadcastChannel<OracleSettings>()
        every { oracle.settingsFlow() } returns channel.asFlow()

        val experimentsManager = ExperimentsSegmentReceivedManager(store, oracle.settingsFlow(), eventsManager)

        channel.send(
            OracleSettings(
                experimentsSegments = mapOf("segment_1" to 2) // a new segment added
            ))

        async {
            experimentsManager.start()
        }

        delay(100)
        channel.close()

        coVerify(exactly = 0) { eventsManager.trackEvent(any<ExperimentSegmentsReceived>()) }
    }

    @Test
    fun `at first time if segments are empty send the event anyway`() = runBlockingTest {

        every { store.loadSegments() } returns null // old settings
        val channel = ConflatedBroadcastChannel<OracleSettings>()
        every { oracle.settingsFlow() } returns channel.asFlow()

        val experimentsManager = ExperimentsSegmentReceivedManager(store, oracle.settingsFlow(), eventsManager)

        channel.send(
            OracleSettings(
                experimentsSegments = mapOf() // a new segment added
            ))

        async {
            experimentsManager.start()
        }

        delay(100)
        channel.close()

        coVerify(exactly = 1) { eventsManager.trackEvent(any<ExperimentSegmentsReceived>()) }
    }

}
