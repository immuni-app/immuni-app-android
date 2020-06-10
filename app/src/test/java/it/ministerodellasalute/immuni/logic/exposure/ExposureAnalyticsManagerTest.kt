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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsNetworkRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.testutils.parseUTCDate
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ExposureAnalyticsManagerTest {
    @MockK
    lateinit var scheduler: ExposureAnalyticsManager.Scheduler
    @MockK
    lateinit var networkRepository: ExposureAnalyticsNetworkRepository
    @MockK
    lateinit var exposureReportingRepository: ExposureReportingRepository
    @MockK
    lateinit var attestationClient: AttestationClient
    @MockK
    lateinit var baseOperationalInfo: BaseOperationalInfo

    lateinit var manager: ExposureAnalyticsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        manager = ExposureAnalyticsManager(
            networkRepository = networkRepository,
            exposureReportingRepository = exposureReportingRepository,
            attestationClient = attestationClient,
            baseOperationalInfoFactory = { baseOperationalInfo },
            schedulerFactory = { scheduler }
        )
    }

    @Test
    fun `exits early when can not send info`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        every { scheduler.couldSendInfo(serverDate) } returns false

        manager.onRequestDiagnosisKeysSucceeded(serverDate)

        verify(exactly = 0) { exposureReportingRepository.getSummaries() }
    }

    @Test
    fun `does not send operational info when it could not send any`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        val spiedManager = spyk(manager)

        every { scheduler.couldSendInfo(serverDate) } returns true
        every { exposureReportingRepository.getSummaries() } returns listOf()
        every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns false
        every { scheduler.couldSendDummyInfoNow(serverDate) } returns false

        manager.onRequestDiagnosisKeysSucceeded(serverDate)

        verify(exactly = 1) { exposureReportingRepository.getSummaries() }

        coVerify(exactly = 0) { spiedManager.sendOperationalInfo(any(), any(), any()) }
    }

    @Test
    fun `sends operational info with exposure when it can`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        val spiedManager = spyk(manager)

        val exposureSummary = mockk<ExposureSummary>()

        every { scheduler.couldSendInfo(serverDate) } returns true
        every { exposureSummary.matchedKeyCount } returns 1
        every { exposureSummary.date } returns serverDate
        every { exposureReportingRepository.getSummaries() } returns listOf(exposureSummary)
        every { scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate) } returns true
        every { scheduler.canSendInfoWithExposure() } returns true
        every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns true
        every { scheduler.couldSendDummyInfoNow(serverDate) } returns true
        coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns Unit
        every { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) } returns Unit

        spiedManager.onRequestDiagnosisKeysSucceeded(serverDate)

        verify(exactly = 1) { exposureReportingRepository.getSummaries() }

        coVerify(exactly = 1) { spiedManager.sendOperationalInfo(exposureSummary, false) }
        coVerify(exactly = 1) { spiedManager.sendOperationalInfo(any(), any()) }
        verify(exactly = 1) { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) }
        verify(exactly = 0) { scheduler.scheduleNextInfoWithoutExposureReport(serverDate) }
        verify(exactly = 0) { scheduler.scheduleNextDummyInfoReport(serverDate) }
    }

    @Test
    fun `sends operational info without exposure when it has already sent those with it this month`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        val spiedManager = spyk(manager)

        val exposureSummary = mockk<ExposureSummary>()

        every { scheduler.couldSendInfo(serverDate) } returns true
        every { exposureSummary.matchedKeyCount } returns 1
        every { exposureSummary.date } returns serverDate
        every { exposureReportingRepository.getSummaries() } returns listOf(exposureSummary)
        every { scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate) } returns false
        every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns true
        every { scheduler.canSendInfoWithoutExposure() } returns true
        every { scheduler.couldSendDummyInfoNow(serverDate) } returns true
        coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns Unit
        every { scheduler.scheduleNextInfoWithoutExposureReport(serverDate) } returns Unit

        spiedManager.onRequestDiagnosisKeysSucceeded(serverDate)

        verify(exactly = 1) { exposureReportingRepository.getSummaries() }

        coVerify(exactly = 1) { spiedManager.sendOperationalInfo(null, false) }
        coVerify(exactly = 1) { spiedManager.sendOperationalInfo(any(), any()) }
        verify(exactly = 0) { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) }
        verify(exactly = 1) { scheduler.scheduleNextInfoWithoutExposureReport(serverDate) }
        verify(exactly = 0) { scheduler.scheduleNextDummyInfoReport(serverDate) }
    }

    @Test
    fun `sends dummy operational info when it has already sent the real ones this month`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        val spiedManager = spyk(manager)

        val exposureSummary = mockk<ExposureSummary>()

        every { scheduler.couldSendInfo(serverDate) } returns true
        every { exposureSummary.matchedKeyCount } returns 1
        every { exposureSummary.date } returns serverDate
        every { exposureReportingRepository.getSummaries() } returns listOf()
        every { scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate) } returns true
        every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns false
        every { scheduler.couldSendDummyInfoNow(serverDate) } returns true
        coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns Unit
        every { scheduler.scheduleNextDummyInfoReport(serverDate) } returns Unit

        spiedManager.onRequestDiagnosisKeysSucceeded(serverDate)

        verify(exactly = 1) { exposureReportingRepository.getSummaries() }

        coVerify(exactly = 1) { spiedManager.sendOperationalInfo(null, true) }
        coVerify(exactly = 1) { spiedManager.sendOperationalInfo(any(), any()) }
        verify(exactly = 0) { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) }
        verify(exactly = 0) { scheduler.scheduleNextInfoWithoutExposureReport(serverDate) }
        verify(exactly = 1) { scheduler.scheduleNextDummyInfoReport(serverDate) }
    }

//    fun `foo`() {
//        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")
//
//        val spiedManager = spyk(manager)
//
//        val exposureSummary = mockk<ExposureSummary>()
//
//        val operationalInfo
//    }
}
