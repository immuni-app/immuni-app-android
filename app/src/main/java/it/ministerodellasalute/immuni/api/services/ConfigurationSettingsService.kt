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
    @field:Json(name = "faq_url") val faqUrls: Map<String, String>,
    @field:Json(name = "tou_url") val termsOfUseUrls: Map<String, String>,
    @field:Json(name = "pn_url") val privacyNoticeUrls: Map<String, String>,
    @field:Json(name = "exposure_configuration") val exposureConfiguration: ExposureConfiguration,
    @field:Json(name = "service_not_active_notification_period") val serviceNotActiveNotificationPeriod: Int,
    @field:Json(name = "onboarding_not_completed_notification_period") val onboardingNotCompletedNotificationPeriod: Int,
    @field:Json(name = "required_update_notification_period") val requiredUpdateNotificationPeriod: Int,
    @field:Json(name = "risk_reminder_notification_period") val riskReminderNotificationPeriod: Int,
    @field:Json(name = "exposure_info_minimum_risk_score") val exposureInfoMinimumRiskScore: Int,
    @field:Json(name = "exposure_detection_period") val exposureDetectionPeriod: Int,
    @field:Json(name = "dummy_teks_average_opportunity_waiting_time") val dummyTeksAverageOpportunityWaitingTime: Int,
    @field:Json(name = "dummy_teks_average_request_waiting_time") val dummyTeksAverageRequestWaitingTime: Int,
    @field:Json(name = "dummy_teks_request_probabilities") val dummyTeksRequestProbabilities: List<Double>,
    @field:Json(name = "teks_max_summary_count") val teksMaxSummaryCount: Int,
    @field:Json(name = "teks_max_info_count") val teksMaxInfoCount: Int,
    @field:Json(name = "teks_packet_size") val teksPacketSize: Int,
    @field:Json(name = "experimental_phase") val experimentalPhase: Boolean = false,
    @field:Json(name = "support_phone_closing_time") val supportPhoneClosingTime: String,
    @field:Json(name = "support_phone_opening_time") val supportPhoneOpeningTime: String,
    @field:Json(name = "support_phone") val supportPhone: String? = null,
    @field:Json(name = "support_email") val supportEmail: String? = null,
    @field:Json(name = "reopen_reminder") val reopenReminder: Boolean = true,
    @field:Json(name = "operational_info_with_exposure_sampling_rate") val operationalInfoWithExposureSamplingRate: Double,
    @field:Json(name = "operational_info_without_exposure_sampling_rate") val operationalInfoWithoutExposureSamplingRate: Double,
    @field:Json(name = "dummy_analytics_waiting_time") val dummyAnalyticsWaitingTime: Int,
    @field:Json(name = "countries") val countries: Map<String, Map<String, String>>,
    @field:Json(name = "eudcc_expiration") val eudcc_expiration: Map<String, Map<String, String>>

)

@JsonClass(generateAdapter = true)
data class ExposureConfiguration(
    @field:Json(name = "attenuation_thresholds") val attenuationThresholds: List<Int>,
    @field:Json(name = "attenuation_bucket_scores") val attenuationScores: List<Int>,
    @field:Json(name = "days_since_last_exposure_bucket_scores") val daysSinceLastExposureScores: List<Int>,
    @field:Json(name = "duration_bucket_scores") val durationScores: List<Int>,
    @field:Json(name = "transmission_risk_bucket_scores") val transmissionRiskScores: List<Int>,
    @field:Json(name = "minimum_risk_score") val minimumRiskScore: Int
)

@JsonClass(generateAdapter = true)
data class Faqs(
    @field:Json(name = "faqs") val faqs: List<Faq>
)

@JsonClass(generateAdapter = true)
data class Faq(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "content") val content: String
)

enum class Language(val code: String) {
    @Json(name = "en")
    EN("en"),

    @Json(name = "it")
    IT("it"),

    @Json(name = "de")
    DE("de"),

    @Json(name = "fr")
    FR("fr"),

    @Json(name = "es")
    ES("es");

    companion object {
        fun fromCode(code: String) = values().firstOrNull { it.code == code } ?: EN
    }
}

private fun languageMap(map: (Language) -> String): Map<String, String> {
    return mapOf(*Language.values().map { language ->
        language.code to map(language)
    }.toTypedArray())
}

private fun countriesMap(): Map<String, Map<String, String>> {
    return mapOf(
        "it" to mapOf(
            "AT" to "AUSTRIA",
            "HR" to "CROAZIA",
            "DK" to "DANIMARCA",
            "EE" to "ESTONIA",
            "DE" to "GERMANIA",
            "IE" to "IRLANDA",
            "LV" to "LETTONIA",
            "NL" to "PAESI BASSI",
            "PL" to "POLONIA",
            "CZ" to "REPUBBLICA CECA",
            "ES" to "SPAGNA"
        ),
        "de" to mapOf(
            "AT" to "ÖSTERREICH",
            "HR" to "KROATIEN",
            "DK" to "DÄNEMARK",
            "EE" to "ESTONIA",
            "DE" to "DEUTSCHLAND",
            "IE" to "IRLAND",
            "LV" to "LETTLAND",
            "NL" to "NIEDERLANDE",
            "PL" to "POLEN",
            "CZ" to "TSCHECHISCHE REPUBLIK",
            "ES" to "SPANIEN"
        ),
        "en" to mapOf(
            "AT" to "AUSTRIA",
            "HR" to "CROATIA",
            "DK" to "DENMARK",
            "EE" to "ESTONIA",
            "DE" to "GERMANY",
            "IE" to "IRELAND",
            "LV" to "LATVIA",
            "NL" to "NETHERLANDS",
            "PL" to "POLAND",
            "CZ" to "CZECH REPUBLIC",
            "ES" to "SPAIN"
        ),
        "fr" to mapOf(
            "AT" to "AUTRICHE",
            "HR" to "CROATIE",
            "DK" to "DANEMARK",
            "EE" to "ESTONIE",
            "DE" to "ALLEMAGNE",
            "IE" to "IRLANDE",
            "LV" to "LETTONIE",
            "NL" to "PAYS-BAS",
            "PL" to "POLOGNE",
            "CZ" to "RÉPUBLIQUE TCHÈQUE",
            "ES" to "ESPAGNE"
        ),
        "es" to mapOf(
            "EN" to "AUSTRIA",
            "HR" to "CROACIA",
            "DK" to "DINAMARCA",
            "EE" to "ESTONIA",
            "DE" to "ALEMANIA",
            "IE" to "IRLANDA",
            "LV" to "LETONIA",
            "NL" to "PAÍSES BAJOS",
            "PL" to "POLONIA",
            "CZ" to "REPÚBLICA CHECA",
            "ES" to "ESPAÑA"
        )
    )
}

private fun eudccMap(): Map<String, Map<String, String>> {
    return mapOf(
        "it" to mapOf(
            "molecular_test" to "Certificazione valida 72 ore dall'ora del prelievo",
            "rapid_test" to "Certificazione valida 48 ore dall'ora del prelievo",
            "vaccine_first_dose" to "Certificazione valida fino alla prossima dose",
            "vaccine_fully_completed" to "Certificazione valida 365 giorni (12 mesi) dalla data dell'ultima somministrazione"
        ),
        "de" to mapOf(
            "molecular_test" to "Bescheinigung gültig für 72 Stunden ab dem Zeitpunkt der Abholung",
            "rapid_test" to "Bescheinigung gültig für 48 Stunden ab dem Zeitpunkt der Abholung",
            "vaccine_first_dose" to "Zertifizierung gültig bis zur nächsten Dosis",
            "vaccine_fully_completed" to "Zertifizierung gültig für 365 Tage (12 Monate) ab dem Datum der letzten Verabreichung"
        ),
        "en" to mapOf(
            "molecular_test" to "Certification valid for 72 hours from the time of collection",
            "rapid_test" to "Certification valid for 48 hours from the time of collection",
            "vaccine_first_dose" to "Certification valid until next dose",
            "vaccine_fully_completed" to "Certification valid for 365 days (12 months) from the date of the last administration"
        ),
        "es" to mapOf(
            "molecular_test" to "Certificación válida por 72 horas desde el momento de la recogida.",
            "rapid_test" to "Certificación válida por 48 horas desde el momento de la recogida.",
            "vaccine_first_dose" to "Certificación válida hasta la próxima dosis",
            "vaccine_fully_completed" to "Certificación válida por 365 días (12 meses) a partir de la fecha de la última administración."
        ),
        "fr" to mapOf(
            "molecular_test" to "Attestation valable 72h à compter de la collecte",
            "rapid_test" to "Attestation valable 48h à compter de la collecte",
            "vaccine_first_dose" to "Certification valable jusqu'à la prochaine dose",
            "vaccine_fully_completed" to "Certification valable 365 jours (12 mois) à compter de la date de la dernière administration"
        )
    )
}
val defaultSettings = ConfigurationSettings(
    minimumBuildVersion = 0,
    faqUrls = languageMap { "https://get.immuni.gov.it/docs/faq-${it.code}.json" },
    termsOfUseUrls = languageMap { "https://www.immuni.italia.it/app-tou.html" },
    privacyNoticeUrls = languageMap { "https://www.immuni.italia.it/app-pn.html" },

    exposureConfiguration = ExposureConfiguration(
        attenuationThresholds = listOf(50, 70),
        attenuationScores = listOf(0, 0, 5, 5, 5, 5, 5, 5),
        daysSinceLastExposureScores = listOf(1, 1, 1, 1, 1, 1, 1, 1),
        durationScores = listOf(0, 0, 0, 0, 5, 5, 5, 5),
        transmissionRiskScores = listOf(1, 1, 1, 1, 1, 1, 1, 1),
        minimumRiskScore = 1
    ),
    exposureInfoMinimumRiskScore = 20,
    exposureDetectionPeriod = 60 * 60 * 4, // 4 hours
    serviceNotActiveNotificationPeriod = 60 * 60 * 24, // 1 day
    onboardingNotCompletedNotificationPeriod = 60 * 60 * 24, // 1 day
    requiredUpdateNotificationPeriod = 60 * 60 * 24, // 1 day
    riskReminderNotificationPeriod = 60 * 60 * 24, // 1 day
    dummyTeksAverageOpportunityWaitingTime = 60 * 24 * 60 * 60, // 60 days
    dummyTeksAverageRequestWaitingTime = 10,
    dummyTeksRequestProbabilities = listOf(0.95, 0.1),
    teksMaxSummaryCount = 6 * 14,
    teksMaxInfoCount = 600,
    teksPacketSize = 110_000,
    experimentalPhase = false,
    supportEmail = null,
    supportPhone = null,
    supportPhoneOpeningTime = "7",
    supportPhoneClosingTime = "22",
    reopenReminder = true,
    operationalInfoWithExposureSamplingRate = 1.0,
    operationalInfoWithoutExposureSamplingRate = 0.6,
    dummyAnalyticsWaitingTime = 2_592_000,
    countries = countriesMap(),
    eudcc_expiration = eudccMap()
)
