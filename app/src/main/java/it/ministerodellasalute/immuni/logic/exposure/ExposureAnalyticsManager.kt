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

import android.util.Base64
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.extensions.utils.byAdding
import it.ministerodellasalute.immuni.extensions.utils.exponential
import it.ministerodellasalute.immuni.logic.exposure.models.AnalyticsTokenStatus
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsNetworkRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsStoreRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import java.security.SecureRandom
import java.util.*

class ExposureAnalyticsManager(
    private val storeRepository: ExposureAnalyticsStoreRepository,
    private val networkRepository: ExposureAnalyticsNetworkRepository,
    private val exposureReportingRepository: ExposureReportingRepository,
    private val settingsManager: ConfigurationSettingsManager,
    private val attestationClient: AttestationClient
) {
    /**
     * Generates random and registers token if needed
     */
    suspend fun setup(serverDate: Date) {
        val token = storeRepository.token
        if (token is AnalyticsTokenStatus.None) {
            storeRepository.token = generateAndValidateToken(serverDate)
        }

        setupInfoWithoutExposureReportingDate(serverDate)
        setupDummyInfoReportingDate(serverDate)
    }

    private fun setupInfoWithoutExposureReportingDate(serverDate: Date) {
        var infoWithoutExposureReportingDate = storeRepository.infoWithoutExposureReportingDate
        if (infoWithoutExposureReportingDate == null) {
            infoWithoutExposureReportingDate = randomDateInMonth(
                serverDate = serverDate,
                isForNextMonth = false
            )
        }
        val infoWithoutExposureExpirationDate = infoWithoutExposureReportingDate.byAdding(hours = 24)
        if (serverDate > infoWithoutExposureExpirationDate) {
            infoWithoutExposureReportingDate = randomDateInMonth(
                serverDate = serverDate,
                isForNextMonth = true
            )
        }
        storeRepository.infoWithoutExposureReportingDate = infoWithoutExposureReportingDate
    }

    private fun setupDummyInfoReportingDate(serverDate: Date) {
        val dummyInfoReportingDate = storeRepository.dummyInfoReportingDate
        if (dummyInfoReportingDate == null || dummyInfoReportingDate < serverDate) {
            scheduleNextDummyInfoReport(serverDate)
        }
    }

    private fun scheduleNextInfoWithoutExposureReport(serverDate: Date) {
        storeRepository.infoWithoutExposureReportingDate = randomDateInMonth(
            serverDate = serverDate,
            isForNextMonth = true
        )
    }

    private fun scheduleNextDummyInfoReport(serverDate: Date) {
        val meanWaitingTime = settingsManager.settings.value.dummyAnalyticsWaitingTime
        val waitingTime = SecureRandom().exponential(meanWaitingTime.toLong()).toInt()
        storeRepository.dummyInfoReportingDate = serverDate.byAdding(seconds = waitingTime)
    }

    suspend fun onRequestDiagnosisKeysSucceeded(serverDate: Date) {
        val exposureSummary = exposureReportingRepository.getSummaries().find {
            serverDate == it.date
        }
        val hadExposure = exposureSummary != null
        val month = monthFromDate(serverDate)
        if (hadExposure && hasYetToSendInfoWithExposureThisMonth(month)) {
            val threshold = settingsManager.settings.value.operationalInfoWithExposureSamplingRate
            val canSend = SecureRandom().nextDouble() < threshold
            if (canSend) {
                // FIXME: send
            }
            storeRepository.infoWithExposureLastReportingMonth = month
        } else if (couldSendInfoWithoutExposureNow(serverDate)) {
            val threshold = settingsManager.settings.value.operationalInfoWithoutExposureSamplingRate
            val canSend = SecureRandom().nextDouble() < threshold
            if (canSend) {
                // FIXME: send
            }
            scheduleNextInfoWithoutExposureReport(serverDate)
        } else if (couldSendDummyInfo(serverDate)) {
            // FIXME: send
            scheduleNextDummyInfoReport(serverDate)
        }
    }

    private fun monthFromDate(serverDate: Date): Int {
        return Calendar.getInstance().apply {
            time = serverDate
        }[Calendar.MONTH]
    }

    private fun hasYetToSendInfoWithExposureThisMonth(month: Int): Boolean {
        return storeRepository.infoWithExposureLastReportingMonth?.let {
            it != month
        } ?: true
    }

    private fun couldSendInfoWithoutExposureNow(serverDate: Date): Boolean {
        return isDateWithin24HoursSinceDate(
            serverDate,
            storeRepository.infoWithoutExposureReportingDate!!
        )
    }

    private fun couldSendDummyInfo(serverDate: Date): Boolean {
        return isDateWithin24HoursSinceDate(
            serverDate,
            storeRepository.dummyInfoReportingDate!!
        )
    }

    private fun isDateWithin24HoursSinceDate(date: Date, periodStartDate: Date): Boolean {
        return date in periodStartDate..periodStartDate.byAdding(hours = 24)
    }

    private suspend fun generateAndValidateToken(serverDate: Date): AnalyticsTokenStatus {
        val token = ByteArray(32)
        SecureRandom().nextBytes(token)
        val base64Token = Base64.encodeToString(token, Base64.DEFAULT)
        val attestationResponse = attestationClient.attest(base64Token)
        return when (attestationResponse) {
            is AttestationClient.Result.Failure -> AnalyticsTokenStatus.None()
            is AttestationClient.Result.Invalid -> AnalyticsTokenStatus.Invalid()
            is AttestationClient.Result.Success -> AnalyticsTokenStatus.Valid(
                base64Token,
                randomDateInMonth(serverDate, isForNextMonth = true)
            )
        }
    }

    private fun randomDateInMonth(serverDate: Date, isForNextMonth: Boolean): Date {
        val lastDayOfCurrentMonth = Calendar.getInstance().apply {
            time = serverDate
            add(Calendar.MONTH, if (isForNextMonth) 2 else 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
        }.get(Calendar.DAY_OF_MONTH)

        val randomDayOfCurrentMonth = SecureRandom().nextInt(lastDayOfCurrentMonth) + 1

        return Calendar.getInstance().apply {
            time = serverDate
            if (isForNextMonth) {
                add(Calendar.MONTH, 1)
            }
            set(Calendar.DAY_OF_MONTH, randomDayOfCurrentMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}
