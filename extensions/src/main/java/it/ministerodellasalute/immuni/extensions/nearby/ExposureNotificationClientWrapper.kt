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

package it.ministerodellasalute.immuni.extensions.nearby

import android.util.Base64
import com.google.android.gms.nearby.exposurenotification.*
import java.io.File
import kotlinx.coroutines.tasks.await

/**
 * Wrapper around [com.google.android.gms.nearby.Nearby] APIs.
 */
class ExposureNotificationClientWrapper(
    private val client: com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
) : ExposureNotificationClient {
    override suspend fun start() {
        client.start().await()
    }

    override suspend fun stop() {
        client.stop().await()
    }

    override fun deviceSupportsLocationlessScanning(): Boolean = client.deviceSupportsLocationlessScanning()

    override suspend fun isEnabled(): Boolean = client.isEnabled.await()

    override suspend fun getTemporaryExposureKeyHistory(): List<ExposureNotificationClient.TemporaryExposureKey> {
        val history = client.temporaryExposureKeyHistory.await()
        return history.map {
            ExposureNotificationClient.TemporaryExposureKey(
                keyData = Base64.encodeToString(it.keyData, Base64.NO_WRAP),
                rollingStartIntervalNumber = it.rollingStartIntervalNumber,
                // TODO: remove once fixed by Google
                // this workaround is needed because of a Google's bug
                rollingPeriod = if (it.rollingPeriod == 0) 144 else it.rollingPeriod,
                transmissionRiskLevel = ExposureNotificationClient.RiskLevel.fromValue(it.transmissionRiskLevel)
            )
        }
    }

    override suspend fun provideDiagnosisKeys(
        keyFiles: List<File>,
        configuration: ExposureNotificationClient.ExposureConfiguration,
        token: String
    ) {
        client.provideDiagnosisKeys(
            keyFiles,
            ExposureConfiguration.ExposureConfigurationBuilder()
                .setDurationAtAttenuationThresholds(*configuration.attenuationThresholds.toIntArray())
                .setMinimumRiskScore(configuration.minimumRiskScore)
                .setAttenuationScores(*configuration.attenuationScores.toIntArray())
                .setDaysSinceLastExposureScores(*configuration.daysSinceLastExposureScores.toIntArray())
                .setDurationScores(*configuration.durationScores.toIntArray())
                .setTransmissionRiskScores(*configuration.transmissionRiskScores.toIntArray())
                .build(),
            token
        ).await()
    }

    override suspend fun getExposureSummary(token: String): ExposureNotificationClient.ExposureSummary {
        val summary = client.getExposureSummary(token).await()
        return ExposureNotificationClient.ExposureSummary(
            daysSinceLastExposure = summary.daysSinceLastExposure,
            matchedKeyCount = summary.matchedKeyCount,
            maximumRiskScore = summary.maximumRiskScore,
            highRiskAttenuationDurationMinutes = summary.attenuationDurationsInMinutes[0],
            mediumRiskAttenuationDurationMinutes = summary.attenuationDurationsInMinutes[1],
            lowRiskAttenuationDurationMinutes = summary.attenuationDurationsInMinutes[2],
            riskScoreSum = summary.summationRiskScore
        )
    }

    override suspend fun getExposureInformation(token: String): List<ExposureNotificationClient.ExposureInformation> {
        val exposureInfo = client.getExposureInformation(token).await()
        return exposureInfo.map {
            ExposureNotificationClient.ExposureInformation(
                dateMillisSinceEpoch = it.dateMillisSinceEpoch,
                durationMinutes = it.durationMinutes,
                attenuationValue = it.attenuationValue,
                transmissionRiskLevel = ExposureNotificationClient.RiskLevel.fromValue(it.transmissionRiskLevel),
                totalRiskScore = it.totalRiskScore,
                highRiskAttenuationDurationMinutes = it.attenuationDurationsInMinutes[0],
                mediumRiskAttenuationDurationMinutes = it.attenuationDurationsInMinutes[1],
                lowRiskAttenuationDurationMinutes = it.attenuationDurationsInMinutes[2]
            )
        }
    }
}
