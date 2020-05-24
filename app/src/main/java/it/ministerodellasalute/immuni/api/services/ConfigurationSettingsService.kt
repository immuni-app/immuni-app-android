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

package it.ministerodellasalute.immuni.api.services

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Configuration Settings Service API.
 */
interface ConfigurationSettingsService {
    @GET("v1/settings?platform=android")
    suspend fun settings(@Query("build") build: Long): Response<ConfigurationSettings>

    @GET
    suspend fun faqs(@Url url: String): Response<Faqs>
}

@JsonClass(generateAdapter = true)
data class ConfigurationSettings(
    @field:Json(name = "minimum_build_version") val minimumBuildVersion: Int,
    @field:Json(name = "faq_url") val faqUrl: Map<Language, String>,
    @field:Json(name = "tos_url") val termsOfServiceUrl: String,
    @field:Json(name = "pp_url") val privacyPolicyUrl: String,
    @field:Json(name = "exposure_configuration") val exposureConfiguration: ExposureConfiguration,
    @field:Json(name = "service_not_active_notification_period") val serviceNotActiveNotificationPeriod: Int, // FIXME use me!
    @field:Json(name = "onboarding_not_completed_notification_period") val onboardingNotCompletedNotificationPeriod: Int,
    @field:Json(name = "required_update_notification_period") val requiredUpdateNotificationPeriod: Int,
    @field:Json(name = "risk_reminder_notification_period") val riskReminderNotificationPeriod: Int, // FIXME use me!
    @field:Json(name = "exposure_info_minimum_risk_score") val exposureInfoMinimumRiskScore: Int,
    @field:Json(name = "exposure_detection_period") val exposureDetectionPeriod: Int,
    @field:Json(name = "support_email") val supportEmail: String = defaultSettings.supportEmail
)

@JsonClass(generateAdapter = true)
class ExposureConfiguration(
    @field:Json(name = "attenuation_thresholds") val attenuationThresholds: List<Int>,
    @field:Json(name = "attenuation_bucket_scores") val attenuationScores: List<Int>,
    @field:Json(name = "days_since_last_exposure_bucket_scores") val daysSinceLastExposureScores: List<Int>,
    @field:Json(name = "duration_bucket_scores") val durationScores: List<Int>,
    @field:Json(name = "transmission_risk_bucket_scores") val transmissionRiskScores: List<Int>,
    @field:Json(name = "minimum_risk_score") val minimumRiskScore: Int
)

@JsonClass(generateAdapter = true)
class Faqs(
    @field:Json(name = "faqs") val faqs: List<Faq>
)

@JsonClass(generateAdapter = true)
class Faq(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "content") val content: String
)

enum class Language(val code: String) {
    @Json(name = "en") EN("en"),
    @Json(name = "it") IT("it"),
    @Json(name = "de") DE("de");

    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: EN
    }
}

// FIXME define sensible defaults!
val defaultSettings = ConfigurationSettings(
    minimumBuildVersion = 0,
    faqUrl = mapOf(
        Language.IT to "",
        Language.DE to "",
        Language.EN to ""
    ),
    termsOfServiceUrl = "",
    privacyPolicyUrl = "",
    supportEmail = "",
    exposureConfiguration = ExposureConfiguration(
        attenuationThresholds = listOf(50, 70),
        attenuationScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        daysSinceLastExposureScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        durationScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        transmissionRiskScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        minimumRiskScore = 1
    ),
    exposureDetectionPeriod = 60 * 60 * 2, // 2 hours
    exposureInfoMinimumRiskScore = 1,
    serviceNotActiveNotificationPeriod = 60 * 60 * 24, // 1 day
    onboardingNotCompletedNotificationPeriod = 60 * 60 * 24, // 1 day
    requiredUpdateNotificationPeriod = 60 * 60 * 24, // 1 day
    riskReminderNotificationPeriod = 60 * 60 * 24 // 1 day
)
