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

package it.ministerodellasalute.immuni.logic.exposure

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import it.ministerodellasalute.immuni.extensions.utils.byAdding
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsStoreRepository
import it.ministerodellasalute.immuni.logic.settings.models.ConfigurationSettings
import it.ministerodellasalute.immuni.testutils.parseUTCDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test

class ExposureAnalyticsSchedulerTest {
    @MockK
    lateinit var storeRepository: ExposureAnalyticsStoreRepository

    @MockK
    lateinit var settings: ConfigurationSettings

    @MockK
    lateinit var random: Random

    private lateinit var scheduler: ExposureAnalyticsManager.Scheduler
    private var infoWithoutExposureReportingDate: Date? = null
    private var dummyInfoReportingDate: Date? = null

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        scheduler = ExposureAnalyticsManager.Scheduler(storeRepository, settings, random)

        infoWithoutExposureReportingDate = null
        dummyInfoReportingDate = null

        every {
            storeRepository.infoWithoutExposureReportingDate
        } answers {
            infoWithoutExposureReportingDate
        }

        every {
            storeRepository.infoWithoutExposureReportingDate = any()
        } nullablePropertyType Date::class answers {
            infoWithoutExposureReportingDate = value
        }

        every {
            storeRepository.dummyInfoReportingDate
        } answers {
            dummyInfoReportingDate
        }

        every {
            storeRepository.dummyInfoReportingDate = any()
        } nullablePropertyType Date::class answers {
            dummyInfoReportingDate = value
        }
    }

    @Test
    fun `when initially run, setup correctly sets the install date`() {
        var installDate: Date? = null
        every { storeRepository.installDate } answers { installDate }
        every {
            storeRepository.installDate = any()
        } nullablePropertyType Date::class answers { installDate = value }

        val serverDate = Date()
        scheduler.setup(serverDate)

        verify(exactly = 1) { storeRepository.installDate = serverDate }
        assertFalse(scheduler.couldSendInfo(serverDate))
    }

    @Test
    fun `setup does nothing if the install date is within 24h`() {
        val serverDate = Date()
        every { storeRepository.installDate } answers { serverDate.byAdding(hours = 23) }

        scheduler.setup(serverDate)
        assertFalse(scheduler.couldSendInfo(serverDate))
    }

    private fun testReportingDate(
        serverDate: Date,
        expectedDate: Date,
        expectedDummyDate: Date,
        shouldSendInfoWithoutExposure: Boolean,
        shouldSendDummyInfo: Boolean
    ) {
        every { storeRepository.installDate } answers { serverDate.byAdding(days = -1) }
        every { settings.dummyAnalyticsWaitingTime } returns 10 * 60 * 60 * 24
        every { random.nextInt(any()) } returns 0
        every { random.nextDouble() } returns 0.1

        scheduler.setup(serverDate)

        verify(exactly = 0) { storeRepository.installDate = serverDate }
        assertTrue(scheduler.couldSendInfo(serverDate))
        assertEquals(expectedDate, infoWithoutExposureReportingDate)
        assertEquals(expectedDummyDate, dummyInfoReportingDate)
        assertEquals(
            shouldSendInfoWithoutExposure,
            scheduler.couldSendInfoWithoutExposureNow(serverDate)
        )
        assertEquals(shouldSendDummyInfo, scheduler.couldSendDummyInfoNow(serverDate))
    }

    @Test
    fun `after 24h the dices are rolled - same month`() {
        var serverDate = parseUTCDate("2020-02-01T00:00:00Z")
        var expectedDate = parseUTCDate("2020-02-01T00:00:00Z")
        var expectedDummyDate = serverDate.byAdding(seconds = 91031)

        testReportingDate(
            serverDate = serverDate,
            expectedDate = expectedDate,
            expectedDummyDate = expectedDummyDate,
            shouldSendInfoWithoutExposure = true,
            shouldSendDummyInfo = false
        )

        expectedDate = parseUTCDate("2020-03-01T00:00:00Z")

        testReportingDate(
            serverDate = serverDate.byAdding(hours = 26),
            expectedDate = expectedDate,
            expectedDummyDate = expectedDummyDate,
            shouldSendInfoWithoutExposure = false,
            shouldSendDummyInfo = true
        )

        serverDate = serverDate.byAdding(hours = 24)

        testReportingDate(
            serverDate = serverDate,
            expectedDate = expectedDate,
            expectedDummyDate = expectedDummyDate,
            shouldSendInfoWithoutExposure = false,
            shouldSendDummyInfo = false
        )

        serverDate = serverDate.byAdding(hours = 26)
        expectedDummyDate = serverDate.byAdding(seconds = 91031)

        testReportingDate(
            serverDate = serverDate,
            expectedDate = expectedDate,
            expectedDummyDate = expectedDummyDate,
            shouldSendInfoWithoutExposure = false,
            shouldSendDummyInfo = false
        )
    }

    @Test
    fun `after 24h the dices are rolled - next month`() {
        val serverDate = parseUTCDate("2020-02-02T00:00:00Z")
        val expectedDate = parseUTCDate("2020-03-01T00:00:00Z")
        val expectedDummyDate = serverDate.byAdding(seconds = 91031)

        testReportingDate(
            serverDate = serverDate,
            expectedDate = expectedDate,
            expectedDummyDate = expectedDummyDate,
            shouldSendInfoWithoutExposure = false,
            shouldSendDummyInfo = false
        )

        testReportingDate(
            serverDate = serverDate.byAdding(hours = 26),
            expectedDate = expectedDate,
            expectedDummyDate = expectedDummyDate,
            shouldSendInfoWithoutExposure = false,
            shouldSendDummyInfo = true
        )
    }

    @Test
    fun `canSendInfoWithExposure is coherent with its setting`() {
        every { settings.operationalInfoWithExposureSamplingRate } returns 0.7
        every { random.nextDouble() } returns 0.6
        assertTrue(scheduler.canSendInfoWithExposure())

        verify { settings.operationalInfoWithExposureSamplingRate }

        every { random.nextDouble() } returns 0.8
        assertFalse(scheduler.canSendInfoWithExposure())
    }

    @Test
    fun `canSendInfoWithoutExposure is coherent with its setting`() {
        every { settings.operationalInfoWithoutExposureSamplingRate } returns 0.2
        every { random.nextDouble() } returns 0.1
        assertTrue(scheduler.canSendInfoWithoutExposure())

        verify { settings.operationalInfoWithoutExposureSamplingRate }

        every { random.nextDouble() } returns 0.3
        assertFalse(scheduler.canSendInfoWithoutExposure())
    }
}
