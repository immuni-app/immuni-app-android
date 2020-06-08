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
import it.ministerodellasalute.immuni.extensions.utils.sha256
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureAnalyticsOperationalInfo
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsNetworkRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsStoreRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import java.security.SecureRandom
import java.util.*

class ExposureAnalyticsManager(
    private val storeRepository: ExposureAnalyticsStoreRepository,
    private val networkRepository: ExposureAnalyticsNetworkRepository,
    private val exposureReportingRepository: ExposureReportingRepository,
    private val settingsManager: ConfigurationSettingsManager,
    private val userManager: UserManager,
    private val attestationClient: AttestationClient,
    private val random: Random = SecureRandom()
) {
    /**
     * Generates random and registers token if needed
     */
    suspend fun setup(serverDate: Date) {
        if (storeRepository.installDate == null) {
            storeRepository.installDate = serverDate
        }

        val isWithin24HoursSinceInstall = storeRepository.installDate!!.byAdding(hours = 24) > serverDate
        if (isWithin24HoursSinceInstall) {
            return
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
        if (isDatePast24HoursSinceDate(serverDate, infoWithoutExposureReportingDate)) {
            infoWithoutExposureReportingDate = randomDateInMonth(
                serverDate = serverDate,
                isForNextMonth = true
            )
        }
        storeRepository.infoWithoutExposureReportingDate = infoWithoutExposureReportingDate
    }

    private fun setupDummyInfoReportingDate(serverDate: Date) {
        val dummyInfoReportingDate = storeRepository.dummyInfoReportingDate
        if (dummyInfoReportingDate == null || isDatePast24HoursSinceDate(serverDate, dummyInfoReportingDate)) {
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
        val waitingTime = random.exponential(meanWaitingTime.toLong()).toInt()
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
            val canSend = random.nextDouble() < threshold
            if (canSend) {
                sendOperationalInfo(summary = exposureSummary, isDummy = false)
            }
            storeRepository.infoWithExposureLastReportingMonth = month
        } else if (couldSendInfoWithoutExposureNow(serverDate)) {
            val threshold = settingsManager.settings.value.operationalInfoWithoutExposureSamplingRate
            val canSend = random.nextDouble() < threshold
            if (canSend) {
                sendOperationalInfo(summary = null, isDummy = false)
            }
            scheduleNextInfoWithoutExposureReport(serverDate)
        } else if (couldSendDummyInfoNow(serverDate)) {
            sendOperationalInfo(summary = null, isDummy = true)
            scheduleNextDummyInfoReport(serverDate)
        }
    }

    private suspend fun sendOperationalInfo(summary: ExposureSummary?, isDummy: Boolean) {
        val operationalInfo = ExposureAnalyticsOperationalInfo(
            province = userManager.user.value!!.province.code,
            exposurePermission = 1, // FIXME
            bluetoothActive = 1, // FIXME
            notificationPermission = 1, // FIXME
            exposureNotification = 1, // FIXME
            lastRiskyExposureOn = (summary?.date ?: Date(0)).let { "VALID_DATE_FROM_$it" }, // FIXME
            token = randomToken()
        )
        val result = attestationClient.attest(operationalInfo.digest.sha256())
        when (result) {
            is AttestationClient.Result.Success -> {
                networkRepository.sendOperationalInfo(operationalInfo)
            }
            is AttestationClient.Result.Invalid -> {}
            is AttestationClient.Result.Failure -> {
                // FIXME: retry N times with exponential backoff
            }
        }
    }

    private fun randomToken(): String {
        val token = ByteArray(16)
        SecureRandom().nextBytes(token)
        return Base64.encodeToString(token, Base64.DEFAULT)
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

    private fun couldSendDummyInfoNow(serverDate: Date): Boolean {
        return isDateWithin24HoursSinceDate(
            serverDate,
            storeRepository.dummyInfoReportingDate!!
        )
    }

    private fun isDateWithin24HoursSinceDate(date: Date, periodStartDate: Date): Boolean {
        return date in periodStartDate..periodStartDate.byAdding(hours = 24)
    }

    private fun isDatePast24HoursSinceDate(date: Date, periodStartDate: Date): Boolean {
        return date > periodStartDate.byAdding(hours = 24)
    }

    private fun randomDateInMonth(serverDate: Date, isForNextMonth: Boolean): Date {
        val lastDayOfCurrentMonth = Calendar.getInstance().apply {
            time = serverDate
            add(Calendar.MONTH, if (isForNextMonth) 2 else 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
        }.get(Calendar.DAY_OF_MONTH)

        val randomDayOfCurrentMonth = random.nextInt(lastDayOfCurrentMonth) + 1

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
