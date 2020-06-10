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
import org.junit.Before
import org.junit.Test
import java.text.DateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExposureAnalyticsSchedulerTest {
    @MockK
    lateinit var storeRepository: ExposureAnalyticsStoreRepository

    @MockK
    lateinit var settings: ConfigurationSettings

    @MockK
    lateinit var random: Random

    private lateinit var scheduler: ExposureAnalyticsManager.Scheduler

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        scheduler = ExposureAnalyticsManager.Scheduler(storeRepository, settings, random)
    }

    @Test
    fun `when initially run, setup correctly sets the install date`() {
        var installDate: Date? = null
        every { storeRepository.installDate } answers { installDate }
        every { storeRepository.installDate = any() } nullablePropertyType Date::class answers { installDate = value }

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

    private fun parseUTCDate(string: String) = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(string)))

    @Test
    fun `after 24h the dices are rolled`() {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")
        val nextMonth = parseUTCDate("2020-03-01T00:00:00Z")

        every { storeRepository.installDate } answers { serverDate.byAdding(days = -1) }
        var infoWithoutExposureReportingDate: Date? = null
        every { storeRepository.infoWithoutExposureReportingDate } answers { infoWithoutExposureReportingDate }
        every { storeRepository.infoWithoutExposureReportingDate = any() } nullablePropertyType Date::class answers { infoWithoutExposureReportingDate = value }
        var dummyInfoReportingDate: Date? = null
        every { storeRepository.dummyInfoReportingDate } answers { dummyInfoReportingDate }
        every { storeRepository.dummyInfoReportingDate = any() } nullablePropertyType Date::class answers { dummyInfoReportingDate = value }
        every { settings.dummyAnalyticsWaitingTime } returns 10 * 60 * 60 * 24
        every { random.nextInt(any()) } returns 0
        every { random.nextDouble() } returns 0.0


        scheduler.setup(serverDate)

        verify(exactly = 0) { storeRepository.installDate = serverDate }
        assertTrue(scheduler.couldSendInfo(serverDate))
        assertEquals(nextMonth, infoWithoutExposureReportingDate)

    }
}
