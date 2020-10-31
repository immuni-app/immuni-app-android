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
import androidx.annotation.VisibleForTesting
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager
import it.ministerodellasalute.immuni.extensions.notifications.PushNotificationManager
import it.ministerodellasalute.immuni.extensions.utils.*
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureAnalyticsOperationalInfo
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsNetworkRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsStoreRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.models.ConfigurationSettings
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.Province
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

class ExposureAnalyticsManager(
    private val settings: StateFlow<ConfigurationSettings>,
    private val networkRepository: ExposureAnalyticsNetworkRepository,
    private val exposureReportingRepository: ExposureReportingRepository,
    private val attestationClient: AttestationClient,
    private val baseOperationalInfoFactory: () -> BaseOperationalInfo,
    private val schedulerFactory: () -> Scheduler,
    private val randomSaltFactory: () -> String = { randomSalt() },
    private val base64Encoder: (ByteArray) -> String = { encodeToBase64String(it) }
) {
    constructor(
        storeRepository: ExposureAnalyticsStoreRepository,
        settingsManager: ConfigurationSettingsManager,
        networkRepository: ExposureAnalyticsNetworkRepository,
        exposureReportingRepository: ExposureReportingRepository,
        attestationClient: AttestationClient,
        baseOperationalInfoFactory: () -> BaseOperationalInfo,
        schedulerFactory: () -> Scheduler = {
            Scheduler(
                storeRepository = storeRepository,
                settings = settingsManager.settings.value
            )
        }
    ) : this(
        settings = settingsManager.settings,
        networkRepository = networkRepository,
        exposureReportingRepository = exposureReportingRepository,
        attestationClient = attestationClient,
        baseOperationalInfoFactory = baseOperationalInfoFactory,
        schedulerFactory = schedulerFactory
    )

    companion object {
        private fun encodeToBase64String(data: ByteArray): String {
            return Base64.encodeToString(data, Base64.NO_WRAP)
        }

        private fun randomSalt(): String {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            return encodeToBase64String(salt)
        }
    }

    fun setup(serverDate: Date) {
        val scheduler = schedulerFactory()
        scheduler.setup(serverDate)
    }

    suspend fun onRequestDiagnosisKeysSucceeded(serverDate: Date) {
        val scheduler = schedulerFactory()
        if (!scheduler.couldSendInfo(serverDate)) {
            return
        }
        val exposureSummary = exposureReportingRepository.getSummaries().find {
            serverDate == it.date
        }
        val hadExposure = exposureSummary != null &&
            exposureSummary.matchedKeyCount > 0 &&
            exposureSummary.maximumRiskScore >= settings.value.exposureInfoMinimumRiskScore

        if (hadExposure && scheduler.hasYetToSendInfoWithExposureThisMonth(serverDate)) {
            if (scheduler.canSendInfoWithExposure()) {
                sendOperationalInfo(summary = exposureSummary, isDummy = false)
            }
            scheduler.updateInfoWithExposureLastReportingMonth(serverDate)
        } else if (scheduler.couldSendInfoWithoutExposureNow(serverDate)) {
            if (scheduler.canSendInfoWithoutExposure()) {
                sendOperationalInfo(summary = null, isDummy = false)
            }
            scheduler.scheduleNextInfoWithoutExposureReport(serverDate)
        } else if (scheduler.couldSendDummyInfoNow(serverDate)) {
            sendOperationalInfo(summary = null, isDummy = true)
            scheduler.scheduleNextDummyInfoReport(serverDate)
        }
    }

    suspend fun sendOperationalInfo(
        summary: ExposureSummary?,
        isDummy: Boolean,
        retryCount: Int = 0
    ): Boolean {
        val baseOperationalInfo = baseOperationalInfoFactory()
        val operationalInfo = ExposureAnalyticsOperationalInfo(
            province = baseOperationalInfo.province,
            exposurePermission = if (baseOperationalInfo.exposurePermission) 1 else 0,
            bluetoothActive = if (baseOperationalInfo.bluetoothActive) 1 else 0,
            notificationPermission = if (baseOperationalInfo.notificationPermission) 1 else 0,
            exposureNotification = if (summary?.let { it.matchedKeyCount > 0 } == true) 1 else 0,
            lastRiskyExposureOn = (summary?.lastExposureDate ?: Date()).isoDateString,
            salt = randomSaltFactory()
        )
        val sha256digest = MessageDigest
            .getInstance("SHA-256")
            .digest(operationalInfo.digest.toByteArray())

        val attestationResult = attestationClient.attest(base64Encoder(sha256digest))
        when (attestationResult) {
            is AttestationClient.Result.Success -> {
                return if (isDummy) {
                    networkRepository.sendDummyOperationalInfo(
                        operationalInfo,
                        attestationResult.result
                    )
                } else {
                    networkRepository.sendOperationalInfo(operationalInfo, attestationResult.result)
                }
            }
            is AttestationClient.Result.Invalid -> {
                return false
            }
            is AttestationClient.Result.Failure -> {
                val newRetryCount = retryCount + 1
                if (newRetryCount > 4) {
                    return false
                }
                val delayMillis = newRetryCount * newRetryCount * 10 * 1000L
                delay(delayMillis)
                return retrySendOperationalInfo(summary, isDummy, newRetryCount)
            }
        }
    }

    @VisibleForTesting
    suspend fun retrySendOperationalInfo(
        summary: ExposureSummary?,
        isDummy: Boolean,
        retryCount: Int
    ): Boolean {
        return sendOperationalInfo(summary, isDummy, retryCount)
    }

    class Scheduler(
        private val storeRepository: ExposureAnalyticsStoreRepository,
        private val settings: ConfigurationSettings,
        private val random: Random = SecureRandom()
    ) {
        fun setup(serverDate: Date) {
            setupInstallDate(serverDate)

            if (isWithin24HoursSinceInstall(serverDate)) {
                return
            }

            setupInfoWithoutExposureReportingDate(serverDate)
            setupDummyInfoReportingDate(serverDate)
        }

        private fun setupInstallDate(serverDate: Date) {
            if (storeRepository.installDate == null) {
                storeRepository.installDate = serverDate
            }
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
            if (dummyInfoReportingDate == null || isDatePast24HoursSinceDate(
                    serverDate,
                    dummyInfoReportingDate
                )
            ) {
                scheduleNextDummyInfoReport(serverDate)
            }
        }

        private fun isWithin24HoursSinceInstall(serverDate: Date): Boolean {
            return storeRepository.installDate!!.byAdding(hours = 24) > serverDate
        }

        fun scheduleNextInfoWithoutExposureReport(serverDate: Date) {
            storeRepository.infoWithoutExposureReportingDate = randomDateInMonth(
                serverDate = serverDate,
                isForNextMonth = true
            )
        }

        fun scheduleNextDummyInfoReport(serverDate: Date) {
            val meanWaitingTime = settings.dummyAnalyticsWaitingTime
            val waitingTime = random.exponential(meanWaitingTime.toLong()).toInt()
            storeRepository.dummyInfoReportingDate = serverDate.byAdding(seconds = waitingTime)
        }

        fun couldSendInfo(serverDate: Date): Boolean {
            return !isWithin24HoursSinceInstall(serverDate)
        }

        fun hasYetToSendInfoWithExposureThisMonth(serverDate: Date): Boolean {
            val month = CalendarUtils.monthFromDate(serverDate)
            return storeRepository.infoWithExposureLastReportingMonth?.let {
                it != month
            } ?: true
        }

        fun updateInfoWithExposureLastReportingMonth(serverDate: Date) {
            storeRepository.infoWithExposureLastReportingMonth =
                CalendarUtils.monthFromDate(serverDate)
        }

        fun couldSendInfoWithoutExposureNow(serverDate: Date): Boolean {
            return CalendarUtils.isDateWithin24HoursSinceDate(
                serverDate,
                storeRepository.infoWithoutExposureReportingDate!!
            )
        }

        fun canSendInfoWithExposure(): Boolean {
            val threshold = settings.operationalInfoWithExposureSamplingRate
            return random.nextDouble() < threshold
        }

        fun canSendInfoWithoutExposure(): Boolean {
            val threshold = settings.operationalInfoWithoutExposureSamplingRate
            return random.nextDouble() < threshold
        }

        fun couldSendDummyInfoNow(serverDate: Date): Boolean {
            return CalendarUtils.isDateWithin24HoursSinceDate(
                serverDate,
                storeRepository.dummyInfoReportingDate!!
            )
        }

        private fun isDatePast24HoursSinceDate(date: Date, periodStartDate: Date): Boolean {
            return date >= periodStartDate.byAdding(hours = 24)
        }

        private fun randomDateInMonth(serverDate: Date, isForNextMonth: Boolean): Date {
            val lastDayOfCurrentMonth = Calendar.getInstance().apply {
                time = serverDate
                add(Calendar.MONTH, if (isForNextMonth) 2 else 1)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.DAY_OF_MONTH, -1)
            }.get(Calendar.DAY_OF_MONTH)

            val randomDayOfCurrentMonth = random.nextInt(lastDayOfCurrentMonth) + 1

            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
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
}

data class BaseOperationalInfo constructor(
    val province: Province,
    val exposurePermission: Boolean,
    val bluetoothActive: Boolean,
    val notificationPermission: Boolean
) {
    constructor(
        userManager: UserManager,
        exposureNotificationManager: ExposureNotificationManager,
        pushNotificationManager: PushNotificationManager
    ) : this(
        province = userManager.user.value!!.province,
        exposurePermission = exposureNotificationManager.areExposureNotificationsEnabled.value
            ?: false,
        bluetoothActive = exposureNotificationManager.bluetoothStateFlow.value,
        notificationPermission = pushNotificationManager.areNotificationsEnabled()
    )
}

object CalendarUtils {
    fun monthFromDate(serverDate: Date): Int {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = serverDate
        }[Calendar.MONTH]
    }

    fun isDateWithin24HoursSinceDate(date: Date, periodStartDate: Date): Boolean {
        return date in periodStartDate..periodStartDate.byAdding(hours = 24)
    }
}
