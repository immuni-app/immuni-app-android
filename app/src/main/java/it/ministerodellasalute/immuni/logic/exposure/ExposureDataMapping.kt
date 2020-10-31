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

import it.ministerodellasalute.immuni.BuildConfig
import it.ministerodellasalute.immuni.api.services.ExposureAnalyticsService
import it.ministerodellasalute.immuni.api.services.ExposureConfiguration
import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.utils.DateUtils
import it.ministerodellasalute.immuni.extensions.utils.isoDateString
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureAnalyticsOperationalInfo
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureInformation
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import java.util.*

val ExposureConfiguration.clientExposureConfiguration: ExposureNotificationClient.ExposureConfiguration
    get() = ExposureNotificationClient.ExposureConfiguration(
        attenuationThresholds = attenuationThresholds,
        minimumRiskScore = minimumRiskScore,
        attenuationScores = attenuationScores,
        daysSinceLastExposureScores = daysSinceLastExposureScores,
        durationScores = durationScores,
        transmissionRiskScores = transmissionRiskScores
    )

val ExposureNotificationClient.TemporaryExposureKey.serviceTemporaryExposureKey
    get() = ExposureIngestionService.TemporaryExposureKey(
        keyData = this.keyData,
        rollingStartIntervalNumber = this.rollingStartIntervalNumber,
        rollingPeriod = this.rollingPeriod
    )

val ExposureInformation.serviceExposureInformation
    get() = ExposureIngestionService.ExposureInformation(
        date = this.date.isoDateString,
        duration = this.durationMinutes * 60,
        attenuationValue = this.attenuationValue,
        transmissionRiskLevel = this.transmissionRiskLevel.value,
        totalRiskScore = this.totalRiskScore,
        attenuationDurations = listOf(
            this.highRiskAttenuationDurationMinutes * 60,
            this.mediumRiskAttenuationDurationMinutes * 60,
            this.lowRiskAttenuationDurationMinutes * 60
        )
    )

fun ExposureSummary.serviceExposureSummary(serverDate: Date): ExposureIngestionService.ExposureSummary {
    val daysSinceLastExposure =
        (serverDate.time - this.lastExposureDate.time) / DateUtils.MILLIS_IN_A_DAY

    return ExposureIngestionService.ExposureSummary(
        date = this.date.isoDateString,
        daysSinceLastExposure = daysSinceLastExposure.toInt(),
        matchedKeyCount = this.matchedKeyCount,
        maximumRiskScore = this.maximumRiskScore,
        attenuationDurations = listOf(
            this.highRiskAttenuationDurationMinutes * 60,
            this.mediumRiskAttenuationDurationMinutes * 60,
            this.lowRiskAttenuationDurationMinutes * 60
        ),
        exposureInfo = this.exposureInfos.map { it.serviceExposureInformation }
    )
}

val ExposureNotificationClient.ExposureInformation.repositoryExposureInformation
    get() = ExposureInformation(
        date = Date(dateMillisSinceEpoch),
        durationMinutes = durationMinutes,
        attenuationValue = attenuationValue,
        transmissionRiskLevel = transmissionRiskLevel,
        totalRiskScore = totalRiskScore,
        highRiskAttenuationDurationMinutes = highRiskAttenuationDurationMinutes,
        mediumRiskAttenuationDurationMinutes = mediumRiskAttenuationDurationMinutes,
        lowRiskAttenuationDurationMinutes = lowRiskAttenuationDurationMinutes
    )

fun ExposureAnalyticsOperationalInfo.operationalInfoRequest(signedAttestation: String): ExposureAnalyticsService.OperationalInfoRequest =
    ExposureAnalyticsService.OperationalInfoRequest(
        province = province.code,
        build = BuildConfig.VERSION_CODE,
        exposurePermission = exposurePermission,
        bluetoothActive = bluetoothActive,
        notificationPermission = notificationPermission,
        exposureNotification = exposureNotification,
        lastRiskyExposureOn = lastRiskyExposureOn,
        salt = salt,
        signedAttestation = signedAttestation
    )
