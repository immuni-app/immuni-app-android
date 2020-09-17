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

import android.content.Context
import android.content.Intent
import java.io.File

interface ExposureNotificationClient {

    enum class Status(val value: Int) {
        SUCCESS(0),
        FAILED_REJECTED_OPT_IN(1),
        FAILED_SERVICE_DISABLED(2),
        FAILED_BLUETOOTH_SCANNING_DISABLED(3),
        FAILED_TEMPORARILY_DISABLED(4),
        FAILED_INSUFFICIENT_STORAGE(5),
        FAILED_INTERNAL(6),
    }

    data class ExposureConfiguration(
        val attenuationThresholds: List<Int>,
        val minimumRiskScore: Int,
        val attenuationScores: List<Int>,
        val daysSinceLastExposureScores: List<Int>,
        val durationScores: List<Int>,
        val transmissionRiskScores: List<Int>
    )

    enum class RiskLevel(val value: Int) {
        INVALID(0),
        LOWEST(1),
        LOW(2),
        LOW_MEDIUM(3),
        MEDIUM(4),
        MEDIUM_HIGH(5),
        HIGH(6),
        VERY_HIGH(7),
        HIGHEST(8);

        companion object {
            fun fromValue(value: Int) = values().first { it.value == value }
        }
    }

    data class TemporaryExposureKey(
        val keyData: String,
        val rollingStartIntervalNumber: Int,
        val rollingPeriod: Int,
        val transmissionRiskLevel: RiskLevel
    )

    data class ExposureSummary(
        val daysSinceLastExposure: Int,
        val matchedKeyCount: Int,
        val maximumRiskScore: Int,
        val highRiskAttenuationDurationMinutes: Int,
        val mediumRiskAttenuationDurationMinutes: Int,
        val lowRiskAttenuationDurationMinutes: Int,
        val riskScoreSum: Int
    )

    data class ExposureInformation(
        val dateMillisSinceEpoch: Long,
        val durationMinutes: Int,
        val attenuationValue: Int,
        val transmissionRiskLevel: RiskLevel,
        val totalRiskScore: Int,
        val highRiskAttenuationDurationMinutes: Int,
        val lowRiskAttenuationDurationMinutes: Int,
        val mediumRiskAttenuationDurationMinutes: Int
    )

    companion object {
        const val ACTION_EXPOSURE_STATE_UPDATED = com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
        const val EXTRA_EXPOSURE_SUMMARY = com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.EXTRA_EXPOSURE_SUMMARY
        const val EXTRA_TOKEN = com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.EXTRA_TOKEN
        const val ACTION_EXPOSURE_NOTIFICATION_SETTINGS = com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient.ACTION_EXPOSURE_NOTIFICATION_SETTINGS

        val exposureNotificationSettingsIntent get() = Intent(ACTION_EXPOSURE_NOTIFICATION_SETTINGS)

        fun hasExposureNotificationSettings(context: Context): Boolean {
            return exposureNotificationSettingsIntent.resolveActivity(context.packageManager) != null
        }
    }

    suspend fun start()

    suspend fun stop()

    suspend fun isEnabled(): Boolean

    fun deviceSupportsLocationlessScanning(): Boolean

    suspend fun getTemporaryExposureKeyHistory(): List<TemporaryExposureKey>

    suspend fun provideDiagnosisKeys(
        keyFiles: List<File>,
        configuration: ExposureConfiguration,
        token: String
    )

    suspend fun getExposureSummary(token: String): ExposureSummary

    suspend fun getExposureInformation(token: String): List<ExposureInformation>
}
