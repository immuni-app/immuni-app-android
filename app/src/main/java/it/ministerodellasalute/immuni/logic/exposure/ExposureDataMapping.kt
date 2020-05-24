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

import it.ministerodellasalute.immuni.api.services.ExposureConfiguration
import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.utils.DateUtils
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureInformation
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import java.text.SimpleDateFormat
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
        date = dateFormatter.format(this.date),
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

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

fun ExposureSummary.serviceExposureSummary(serverDate: Date): ExposureIngestionService.ExposureSummary {
    val daysSinceLastExposure =
        (serverDate.time - this.lastExposureDate.time) / DateUtils.MILLIS_IN_A_DAY

    return ExposureIngestionService.ExposureSummary(
        date = dateFormatter.format(this.date),
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
