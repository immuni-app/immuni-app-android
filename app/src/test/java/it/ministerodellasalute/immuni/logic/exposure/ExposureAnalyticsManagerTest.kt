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
import it.ministerodellasalute.immuni.api.services.defaultSettings
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.extensions.utils.byAdding
import it.ministerodellasalute.immuni.extensions.utils.isoDateString
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureAnalyticsOperationalInfo
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsNetworkRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.user.models.Province
import it.ministerodellasalute.immuni.testutils.parseUTCDate
import java.util.*
import kotlin.test.assertEquals
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
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
    lateinit var baseOperationalInfoFactory: () -> BaseOperationalInfo

    @MockK
    lateinit var randomSaltFactory: () -> String

    private lateinit var manager: ExposureAnalyticsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        manager = ExposureAnalyticsManager(
            settings = MutableStateFlow(defaultSettings),
            networkRepository = networkRepository,
            exposureReportingRepository = exposureReportingRepository,
            attestationClient = attestationClient,
            baseOperationalInfoFactory = baseOperationalInfoFactory,
            schedulerFactory = { scheduler },
            randomSaltFactory = randomSaltFactory,
            base64Encoder = { Base64.getEncoder().encodeToString(it) }
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
    fun `does not send operational info with exposure when maximum risk score is below the threshold`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        val spiedManager = spyk(manager)

        val exposureSummary = mockk<ExposureSummary>()

        every { scheduler.couldSendInfo(serverDate) } returns true
        every { exposureSummary.matchedKeyCount } returns 1
        every { exposureSummary.maximumRiskScore } returns 10
        every { exposureSummary.date } returns serverDate
        every { exposureReportingRepository.getSummaries() } returns listOf(exposureSummary)
        every { scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate) } returns true
        every { scheduler.canSendInfoWithExposure() } returns true
        every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns false
        every { scheduler.couldSendDummyInfoNow(serverDate) } returns false
        coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns true
        every { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) } returns Unit

        spiedManager.onRequestDiagnosisKeysSucceeded(serverDate)

        verify(exactly = 1) { exposureReportingRepository.getSummaries() }

        coVerify(exactly = 0) { spiedManager.sendOperationalInfo(exposureSummary, false) }
        coVerify(exactly = 0) { spiedManager.sendOperationalInfo(any(), any()) }
        verify(exactly = 0) { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) }
        verify(exactly = 0) { scheduler.scheduleNextInfoWithoutExposureReport(serverDate) }
        verify(exactly = 0) { scheduler.scheduleNextDummyInfoReport(serverDate) }
    }

    @Test
    fun `sends operational info with exposure when it can`() = runBlocking {
        val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

        val spiedManager = spyk(manager)

        val exposureSummary = mockk<ExposureSummary>()

        every { scheduler.couldSendInfo(serverDate) } returns true
        every { exposureSummary.matchedKeyCount } returns 1
        every { exposureSummary.maximumRiskScore } returns 30
        every { exposureSummary.date } returns serverDate
        every { exposureReportingRepository.getSummaries() } returns listOf(exposureSummary)
        every { scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate) } returns true
        every { scheduler.canSendInfoWithExposure() } returns true
        every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns true
        every { scheduler.couldSendDummyInfoNow(serverDate) } returns true
        coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns true
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
    fun `sends operational info without exposure when it has already sent those with it this month`() =
        runBlocking {
            val serverDate = parseUTCDate("2020-02-01T00:00:00Z")

            val spiedManager = spyk(manager)

            val exposureSummary = mockk<ExposureSummary>()

            every { scheduler.couldSendInfo(serverDate) } returns true
            every { exposureSummary.matchedKeyCount } returns 1
            every { exposureSummary.maximumRiskScore } returns 50
            every { exposureSummary.date } returns serverDate
            every { exposureReportingRepository.getSummaries() } returns listOf(exposureSummary)
            every { scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate) } returns false
            every { scheduler.couldSendInfoWithoutExposureNow(serverDate) } returns true
            every { scheduler.canSendInfoWithoutExposure() } returns true
            every { scheduler.couldSendDummyInfoNow(serverDate) } returns true
            coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns true
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
    fun `sends dummy operational info when it has already sent the real ones this month`() =
        runBlocking {
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
            coEvery { spiedManager.sendOperationalInfo(any(), any()) } returns true
            every { scheduler.scheduleNextDummyInfoReport(serverDate) } returns Unit

            spiedManager.onRequestDiagnosisKeysSucceeded(serverDate)

            verify(exactly = 1) { exposureReportingRepository.getSummaries() }

            coVerify(exactly = 1) { spiedManager.sendOperationalInfo(null, true) }
            coVerify(exactly = 1) { spiedManager.sendOperationalInfo(any(), any()) }
            verify(exactly = 0) { scheduler.updateInfoWithExposureLastReportingMonth(serverDate) }
            verify(exactly = 0) { scheduler.scheduleNextInfoWithoutExposureReport(serverDate) }
            verify(exactly = 1) { scheduler.scheduleNextDummyInfoReport(serverDate) }
        }

    @Test
    fun `dummy operational info is sent correctly`() = runBlocking {
        every { baseOperationalInfoFactory() } returns BaseOperationalInfo(
            province = Province.agrigento,
            exposurePermission = true,
            bluetoothActive = true,
            notificationPermission = true
        )
        val salt = "1234567812345678"
        every { randomSaltFactory() } returns salt
        val successString = "SUCCESS_STRING"
        coEvery {
            attestationClient.attest(any())
        } returns AttestationClient.Result.Success(successString)
        coEvery { networkRepository.sendDummyOperationalInfo(any(), successString) } returns true

        manager.sendOperationalInfo(
            summary = null,
            isDummy = true
        )
        val operationalInfoSlot = slot<ExposureAnalyticsOperationalInfo>()
        coVerify(exactly = 1) {
            networkRepository.sendDummyOperationalInfo(
                capture(operationalInfoSlot),
                successString
            )
        }
        val operationalInfo = operationalInfoSlot.captured
        assertEquals(1, operationalInfo.exposurePermission)
        assertEquals(1, operationalInfo.bluetoothActive)
        assertEquals(1, operationalInfo.notificationPermission)
        assertEquals(0, operationalInfo.exposureNotification)
        assertEquals(salt, operationalInfo.salt)
        assertEquals(Province.agrigento, operationalInfo.province)
    }

    @Test
    fun `operational info without exposure (no summary) is sent correctly`() = runBlocking {
        every { baseOperationalInfoFactory() } returns BaseOperationalInfo(
            province = Province.cagliari,
            exposurePermission = true,
            bluetoothActive = false,
            notificationPermission = false
        )
        val salt = "1234567812345678"
        every { randomSaltFactory() } returns salt
        val successString = "SUCCESS_STRING"
        coEvery {
            attestationClient.attest(any())
        } returns AttestationClient.Result.Success(successString)
        coEvery { networkRepository.sendOperationalInfo(any(), successString) } returns true

        manager.sendOperationalInfo(
            summary = null,
            isDummy = false
        )
        val operationalInfoSlot = slot<ExposureAnalyticsOperationalInfo>()
        coVerify(exactly = 1) {
            networkRepository.sendOperationalInfo(
                capture(operationalInfoSlot),
                successString
            )
        }
        val operationalInfo = operationalInfoSlot.captured
        assertEquals(1, operationalInfo.exposurePermission)
        assertEquals(0, operationalInfo.bluetoothActive)
        assertEquals(0, operationalInfo.notificationPermission)
        assertEquals(0, operationalInfo.exposureNotification)
        assertEquals(salt, operationalInfo.salt)
        assertEquals(Province.cagliari, operationalInfo.province)
    }

    @Test
    fun `operational info without exposure (empty summary) is sent correctly`() = runBlocking {
        every { baseOperationalInfoFactory() } returns BaseOperationalInfo(
            province = Province.cagliari,
            exposurePermission = true,
            bluetoothActive = false,
            notificationPermission = false
        )
        val salt = "1234567812345678"
        every { randomSaltFactory() } returns salt
        val successString = "SUCCESS_STRING"
        coEvery {
            attestationClient.attest(any())
        } returns AttestationClient.Result.Success(successString)
        coEvery { networkRepository.sendOperationalInfo(any(), successString) } returns true

        val summary = ExposureSummary(
            date = Date(),
            lastExposureDate = Date(),
            matchedKeyCount = 0,
            maximumRiskScore = 0,
            highRiskAttenuationDurationMinutes = 0,
            mediumRiskAttenuationDurationMinutes = 0,
            lowRiskAttenuationDurationMinutes = 0,
            riskScoreSum = 0,
            exposureInfos = listOf()
        )
        manager.sendOperationalInfo(
            summary = summary,
            isDummy = false
        )
        val operationalInfoSlot = slot<ExposureAnalyticsOperationalInfo>()
        coVerify(exactly = 1) {
            networkRepository.sendOperationalInfo(
                capture(operationalInfoSlot),
                successString
            )
        }
        val operationalInfo = operationalInfoSlot.captured
        assertEquals(1, operationalInfo.exposurePermission)
        assertEquals(0, operationalInfo.bluetoothActive)
        assertEquals(0, operationalInfo.notificationPermission)
        assertEquals(0, operationalInfo.exposureNotification)
        assertEquals(salt, operationalInfo.salt)
        assertEquals(Province.cagliari, operationalInfo.province)
    }

    @Test
    fun `operational info with exposure is sent correctly`() = runBlocking {
        every { baseOperationalInfoFactory() } returns BaseOperationalInfo(
            province = Province.cagliari,
            exposurePermission = true,
            bluetoothActive = false,
            notificationPermission = false
        )
        val salt = "1234567812345678"
        every { randomSaltFactory() } returns salt
        val successString = "SUCCESS_STRING"
        coEvery {
            attestationClient.attest(any())
        } returns AttestationClient.Result.Success(successString)
        coEvery { networkRepository.sendOperationalInfo(any(), successString) } returns true
        val lastExposureDate = Date().byAdding(days = -2)
        val summary = ExposureSummary(
            date = Date(),
            lastExposureDate = lastExposureDate,
            matchedKeyCount = 1,
            maximumRiskScore = 100,
            highRiskAttenuationDurationMinutes = 15,
            mediumRiskAttenuationDurationMinutes = 15,
            lowRiskAttenuationDurationMinutes = 0,
            riskScoreSum = 25,
            exposureInfos = listOf()
        )

        manager.sendOperationalInfo(
            summary = summary,
            isDummy = false
        )
        val operationalInfoSlot = slot<ExposureAnalyticsOperationalInfo>()
        coVerify(exactly = 1) {
            networkRepository.sendOperationalInfo(
                capture(operationalInfoSlot),
                successString
            )
        }
        val operationalInfo = operationalInfoSlot.captured
        assertEquals(1, operationalInfo.exposurePermission)
        assertEquals(0, operationalInfo.bluetoothActive)
        assertEquals(0, operationalInfo.notificationPermission)
        assertEquals(1, operationalInfo.exposureNotification)
        assertEquals(lastExposureDate.isoDateString, operationalInfo.lastRiskyExposureOn)
        assertEquals(salt, operationalInfo.salt)
        assertEquals(Province.cagliari, operationalInfo.province)
    }

    @Test
    fun `exponential backoff on attestation failure`() = runBlockingTest {
        pauseDispatcher {
            every { baseOperationalInfoFactory() } returns BaseOperationalInfo(
                province = Province.cagliari,
                exposurePermission = true,
                bluetoothActive = false,
                notificationPermission = false
            )
            val salt = "1234567812345678"
            every { randomSaltFactory() } returns salt
            val successString = "SUCCESS_STRING"
            val error = Exception()
            var callCount = 0
            coEvery {
                attestationClient.attest(any())
            } answers {
                callCount += 1
                if (callCount == 5) AttestationClient.Result.Success(successString)
                else AttestationClient.Result.Failure(error)
            }
            coEvery {
                networkRepository.sendDummyOperationalInfo(
                    any(),
                    successString
                )
            } returns true
            val mockManager = spyk(manager)

            val job = async {
                mockManager.sendOperationalInfo(
                    summary = null,
                    isDummy = true
                )
            }
            advanceTimeBy(5 * 60 * 1000)
            coVerifyOrder {
                mockManager.sendOperationalInfo(any(), true, 0)
                mockManager.retrySendOperationalInfo(any(), true, 1)
                mockManager.retrySendOperationalInfo(any(), true, 2)
                mockManager.retrySendOperationalInfo(any(), true, 3)
                mockManager.retrySendOperationalInfo(any(), true, 4)
            }
            coVerify(exactly = 0) {
                mockManager.retrySendOperationalInfo(any(), true, 5)
            }
            coVerify(exactly = 1) {
                networkRepository.sendDummyOperationalInfo(
                    any(),
                    successString
                )
            }

            job.await()
        }
    }

    @Test
    fun `does not send and does not retry with invalid attestation`() = runBlockingTest {
        every { baseOperationalInfoFactory() } returns BaseOperationalInfo(
            province = Province.cagliari,
            exposurePermission = true,
            bluetoothActive = false,
            notificationPermission = false
        )
        val salt = "1234567812345678"
        every { randomSaltFactory() } returns salt

        coEvery {
            attestationClient.attest(any())
        } returns AttestationClient.Result.Invalid

        manager.sendOperationalInfo(
            summary = null,
            isDummy = true
        )

        coVerify(exactly = 0) { networkRepository.sendOperationalInfo(any(), any()) }
        coVerify(exactly = 0) { networkRepository.sendDummyOperationalInfo(any(), any()) }
    }
}
