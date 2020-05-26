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

import android.app.Activity
import android.content.Intent
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import it.ministerodellasalute.immuni.logic.exposure.models.OtpToken
import it.ministerodellasalute.immuni.logic.exposure.models.OtpValidationResult
import it.ministerodellasalute.immuni.logic.exposure.repositories.*
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.repositories.UserRepository
import java.io.File
import java.util.*
import kotlin.math.max
import kotlinx.coroutines.flow.*

class ExposureManager(
    private val notificationManager: AppNotificationManager,
    private val settingsManager: ConfigurationSettingsManager,
    private val exposureNotificationManager: ExposureNotificationManager,
    private val userRepository: UserRepository,
    private val exposureReportingRepository: ExposureReportingRepository,
    private val exposureIngestionRepository: ExposureIngestionRepository,
    private val exposureStatusRepository: ExposureStatusRepository
) : ExposureNotificationManager.Delegate {

    companion object {
        const val HIGH_RISK_ATTENUATION_DURATION_MINUTES = 15
    }

    private val settings get() = settingsManager.settings.value

    val isBroadcastingActive: StateFlow<Boolean?> = exposureNotificationManager.isBroadcastingActive

    init {
        exposureNotificationManager.setup(this)
    }

    val exposureStatus = exposureStatusRepository.exposureStatus

    override suspend fun processKeys(
        serverDate: Date,
        summary: ExposureNotificationClient.ExposureSummary,
        getInfos: suspend () -> List<ExposureNotificationClient.ExposureInformation>
    ) {
        val lastExposureDate = Calendar.getInstance().apply {
            time = serverDate
            add(Calendar.DAY_OF_YEAR, -summary.daysSinceLastExposure)
        }.time

        var summaryEntity =
            ExposureSummary(
                date = serverDate,
                lastExposureDate = lastExposureDate,
                matchedKeyCount = summary.matchedKeyCount,
                maximumRiskScore = summary.maximumRiskScore,
                highRiskAttenuationDurationMinutes = summary.highRiskAttenuationDurationMinutes,
                mediumRiskAttenuationDurationMinutes = summary.mediumRiskAttenuationDurationMinutes,
                lowRiskAttenuationDurationMinutes = summary.lowRiskAttenuationDurationMinutes,
                riskScoreSum = summary.riskScoreSum
            )

        val oldExposureStatus = exposureStatusRepository.exposureStatus.value

        val newExposureStatus = computeExposureStatus(summaryEntity, oldExposureStatus)

        if (shouldSendNotification(oldExposureStatus, newExposureStatus)) {
            exposureStatusRepository.setExposureStatus(newExposureStatus)
            val infos = getInfos()
            summaryEntity = summaryEntity.copy(
                exposureInfos = infos.map { it.repositoryExposureInformation }
            )

            notificationManager.triggerNotification(NotificationType.Exposure)
        }

        exposureReportingRepository.addSummary(summaryEntity)
    }

    private fun computeExposureStatus(
        summary: ExposureSummary,
        oldExposureStatus: ExposureStatus
    ): ExposureStatus {
        if (summary.matchedKeyCount == 0 || summary.highRiskAttenuationDurationMinutes < HIGH_RISK_ATTENUATION_DURATION_MINUTES ||
            summary.maximumRiskScore < settings.exposureInfoMinimumRiskScore) {
            return oldExposureStatus
        }
        val oldStatusLastExposureTime =
            (oldExposureStatus as? ExposureStatus.Exposed)?.lastExposureDate?.time
        val maxLastExposureDate =
            Date(max(summary.lastExposureDate.time, oldStatusLastExposureTime ?: 0))

        return when (oldExposureStatus) {
            is ExposureStatus.Positive ->
                oldExposureStatus
            is ExposureStatus.Exposed ->
                ExposureStatus.Exposed(lastExposureDate = maxLastExposureDate)
            is ExposureStatus.None ->
                ExposureStatus.Exposed(lastExposureDate = maxLastExposureDate)
        }
    }

    private fun shouldSendNotification(old: ExposureStatus, new: ExposureStatus): Boolean {
        return when {
            old is ExposureStatus.None
                && new is ExposureStatus.Exposed -> {
                true
            }
            old is ExposureStatus.Exposed &&
                new is ExposureStatus.Exposed
                && new.lastExposureDate > old.lastExposureDate -> {
                true
            }
            else -> false
        }
    }

    suspend fun optInAndStartExposureTracing(activity: Activity) {
        stopExposureNotification()
        exposureNotificationManager.optInAndStartExposureTracing(activity)
    }

    suspend fun stopExposureNotification() {
        exposureNotificationManager.stopExposureNotification()
    }

    suspend fun provideDiagnosisKeys(keyFiles: List<File>, token: String) {
        exposureNotificationManager.provideDiagnosisKeys(
            keyFiles = keyFiles,
            configuration = settings.exposureConfiguration.clientExposureConfiguration,
            token = token
        )
    }

    suspend fun startProcessingKeys(token: String, serverDate: Date) {
        exposureNotificationManager.processKeys(token, serverDate)
    }

    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        exposureNotificationManager.onRequestPermissionsResult(
            activity = activity,
            requestCode = requestCode,
            resultCode = resultCode,
            data = data
        )
    }

    suspend fun requestTekHistory(activity: Activity): List<ExposureNotificationClient.TemporaryExposureKey> {
        if (exposureNotificationManager.areExposureNotificationsEnabled.value != true) {
            exposureNotificationManager.optInAndStartExposureTracing(activity)
        }
        return exposureNotificationManager.requestTekHistory(activity)
    }

    suspend fun validateOtp(otp: String): OtpValidationResult {
        return exposureIngestionRepository.validateOtp(otp, isDummy = false)
    }

    suspend fun dummyUpload(): OtpValidationResult {
        val otp = "DUMMY"
        return exposureIngestionRepository.validateOtp(otp, isDummy = true)
    }

    suspend fun uploadTeks(activity: Activity, token: OtpToken): Boolean {
        val tekHistory = requestTekHistory(activity)
        val exposureSummaries = exposureReportingRepository.getSummaries()

        val isSuccess = exposureIngestionRepository.uploadTeks(
            token = token,
            province = userRepository.user.value!!.province,
            tekHistory = tekHistory.map { it.serviceTemporaryExposureKey },
            exposureSummaries = exposureSummaries.map {
                it.serviceExposureSummary(serverDate = token.serverDate)
            }
        )

        if (isSuccess) {
            exposureStatusRepository.setExposureStatus(ExposureStatus.Positive())
        }

        return isSuccess
    }

    fun resetExposureStatus() {
        exposureStatusRepository.resetExposureStatus()
        exposureStatusRepository.mockExposureStatus = null
    }

    fun debugCleanupDatabase() {
        exposureReportingRepository.resetSummaries()
        exposureReportingRepository.setLastProcessedChunk(null)
    }

    fun setMockExposureStatus(status: ExposureStatus?) {
        exposureStatusRepository.mockExposureStatus = status
    }

    val hasSummaries: Boolean get() = exposureReportingRepository.getSummaries().isNotEmpty()
}
