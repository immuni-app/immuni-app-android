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
    @field:Json(name = "eudcc_expiration") val eudcc_expiration: Map<String, Map<String, String>>,
    @field:Json(name = "risk_exposure") val risk_exposure: Map<String, String>
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
            "vaccine_fully_completed" to "Certificazione valida 365 giorni (12 mesi) dalla data dell'ultima somministrazione",
            "healing_certificate" to "Certificazione valida in Unione Europea fino alla data di fine validità e valida solo in Italia fino a 6 mesi dalla data di inizio validità",
            "vaccine_booster" to "Certificazione valida 180 giorni (6 mesi) dalla data dell'ultima somministrazione, salvo modifiche normative",
            "cbis" to "Certificazione valida in Unione Europea fino alla data di fine validità e valida in Italia 540 giorni (18 mesi) dalla data di inizio validità, salvo modifiche normative"
        ),
        "de" to mapOf(
            "molecular_test" to "Bescheinigung gültig für 72 Stunden ab dem Zeitpunkt der Abholung",
            "rapid_test" to "Bescheinigung gültig für 48 Stunden ab dem Zeitpunkt der Abholung",
            "vaccine_first_dose" to "Zertifizierung gültig bis zur nächsten Dosis",
            "vaccine_fully_completed" to "Zertifizierung gültig für 365 Tage (12 Monate) ab dem Datum der letzten Verabreichung",
            "healing_certificate" to "Zertifizierung gültig in der Europäischen Union bis zum Gültigkeitsende und nur in Italien bis zu 6 Monate ab Gültigkeitsbeginn gültig",
            "vaccine_booster" to "Zertifizierung gültig für 180 Tage (6 Monate) ab dem Datum der letzten Verabreichung, vorbehaltlich behördlicher Änderungen",
            "cbis" to "Zertifizierung gültig in der Europäischen Union bis Gültigkeitsende und gültig in Italien 540 Tage (18 Monate) ab Gültigkeitsbeginn, vorbehaltlich behördlicher Änderungen"
        ),
        "en" to mapOf(
            "molecular_test" to "Certification valid for 72 hours from the time of collection",
            "rapid_test" to "Certification valid for 48 hours from the time of collection",
            "vaccine_first_dose" to "Certification valid until next dose",
            "vaccine_fully_completed" to "Certification valid for 365 days (12 months) from the date of the last administration",
            "healing_certificate" to "Certification valid in the European Union until the end of validity date and valid only in Italy up to 6 months from the start of validity date",
            "vaccine_booster" to "Certification valid for 180 days (6 months) from the date of the last administration, subject to regulatory changes",
            "cbis" to "Certification valid in the European Union until the end of validity date and valid in Italy 540 days (18 months) from the start of validity date, subject to regulatory changes"
        ),
        "es" to mapOf(
            "molecular_test" to "Certificación válida por 72 horas desde el momento de la recogida.",
            "rapid_test" to "Certificación válida por 48 horas desde el momento de la recogida.",
            "vaccine_first_dose" to "Certificación válida hasta la próxima dosis",
            "vaccine_fully_completed" to "Certificación válida por 365 días (12 meses) a partir de la fecha de la última administración.",
            "healing_certificate" to "Certificación válida en la Unión Europea hasta el final de la fecha de validez y válida solo en Italia hasta 6 meses desde el inicio de la fecha de validez",
            "vaccine_booster" to "Certificación válida por 180 días (6 meses) a partir de la fecha de la última administración, sujeta a cambios regulatorios",
            "cbis" to "Certificación válida en la Unión Europea hasta el final de la fecha de validez y válida en Italia 540 días (18 meses) desde el inicio de la fecha de validez, sujeta a cambios regulatorios"
        ),
        "fr" to mapOf(
            "molecular_test" to "Attestation valable 72h à compter de la collecte",
            "rapid_test" to "Attestation valable 48h à compter de la collecte",
            "vaccine_first_dose" to "Certification valable jusqu'à la prochaine dose",
            "vaccine_fully_completed" to "Certification valable 365 jours (12 mois) à compter de la date de la dernière administration",
            "healing_certificate" to "Certification valable dans l'Union européenne jusqu'à la date de fin de validité et valable uniquement en Italie jusqu'à 6 mois à compter de la date de début de validité",
            "vaccine_booster" to "Certification valable 180 jours (6 mois) à compter de la date de la dernière administration, sous réserve de modifications réglementaires",
            "cbis" to "Certification valable dans l'Union européenne jusqu'à la date de fin de validité et valable en Italie 540 jours (18 mois) à compter de la date de début de validité, sous réserve de modifications réglementaires"
        )
    )
}

fun risk_exposure(): Map<String, String> {
    return mapOf(
        "it" to "Se non hai sintomi e hai ricevuto la dose booster oppure hai completato il ciclo vaccinale primario nei 120 giorni precedenti, oppure sei guarito da infezione da SARS-CoV-2 nei 120 giorni precedenti, oppure sei guarito dopo il completamento del ciclo primario, non è prevista la quarantena e si applica la misura dell’autosorveglianza della durata di 5 giorni. \n \n Alla prima comparsa di sintomi effettua un test antigenico rapido o molecolare per la rilevazione di Sars-Cov-2 e, se ancora sintomatico, al quinto giorno successivo alla data dell’ultimo contatto stretto con soggetti confermati positivi al Covid 19. Indossa dispositivi di protezione delle vie respiratorie di tipo FFP2 per almeno 10 giorni dall’ultima esposizione al caso.\n \n Se non hai sintomi e non sei vaccinato o non hai completato il ciclo vaccinale primario (hai ricevuto una sola dose di vaccino delle due previste) o se hai completato il ciclo vaccinale primario da meno di 14 giorni, oppure sei asintomatico e hai completato il ciclo vaccinale primario o sei guarito da precedente infezione da SARS-CoV-2 da più di 120 giorni senza aver ricevuto la dose di richiamo, rimani a casa per la durata della quarantena di 5 giorni dall’ultimo contatto con il caso positivo. Dopo tale periodo devi effettuare un test antigenico rapido o molecolare. Se il risultato è negativo, la quarantena cessa ma per i cinque giorni successivi devi indossare i dispositivi di protezione FFP2.\n \n Se durante il periodo di quarantena manifesti sintomi suggestivi di possibile infezione da Sars-Cov-2 è raccomandata l’esecuzione immediata di un test diagnostico.",
        "en" to "You are not quarantine is foreseen and the self-surveillance measure lasting 5 days is applied. \n \n At the first appearance of symptoms carry out a rapid or molecular antigen test for the detection of Sars-Cov-2 and, if still symptomatic, on the fifth day following the date of the last close contact with subjects confirmed positive for Covid 19. Wear devices FFP2 respiratory protection for at least 10 days from the last exposure to the case. \n \n If you have no symptoms and are not vaccinated or have not completed the primary vaccination course (you have received only one of the two vaccine doses) or if you have completed the primary vaccination course for less than 14 days, or are asymptomatic and have completed the primary vaccination course or have recovered from a previous SARS-CoV-2 infection for more than 120 days without receiving the booster dose, stay at house for the duration of the quarantine of 5 days from the last contact with the positive case. After this time, you need to do a rapid or molecular antigen test. If the result is negative, the quarantine ends but you must wear FFP2 protective equipment for the next five days. \n \n If during the quarantine period you experience symptoms suggestive of possible Sars-Cov-2 infection, immediate execution of the a diagnostic test.",
        "es" to "No está prevista la cuarentena y se aplica la medida de autovigilancia de 5 días. \n \n A la primera aparición de síntomas realizar una prueba rápida o de antígeno molecular para la detección de Sars-Cov-2 y, si continúa sintomático, al quinto día siguiente a la fecha del último contacto cercano con sujetos confirmados positivos para Covid 19 .Usar dispositivos de protección respiratoria FFP2 durante al menos 10 días desde la última exposición al caso.\n \n Si no tiene síntomas y no está vacunado o no ha completado el ciclo de vacunación primaria (ha recibido solo una de las dos dosis de vacuna) o si completó el ciclo de vacunación primaria durante menos de 14 días, o está asintomático y completó el ciclo de vacunación primaria o se recuperó de una infección previa por SARS-CoV-2 durante más de 120 días sin recibir la dosis de refuerzo, quédese en casa mientras dure la cuarentena de 5 días a partir del último contacto con el caso positivo. Pasado este tiempo, es necesario realizar una prueba rápida o de antígeno molecular. Si el resultado es negativo, finaliza la cuarentena pero debe llevar equipo de protección FFP2 durante los cinco días siguientes.\n \n Si durante el periodo de cuarentena presenta síntomas sugestivos de posible infección por Sars-Cov-2, realización inmediata de una prueba diagnóstica.",
        "fr" to "Vous n'êtes pas en quarantaine est prévue et la mesure d'auto-surveillance d'une durée de 5 jours est appliquée. \n \n À la première apparition des symptômes effectuer un test antigénique rapide ou moléculaire pour la détection du Sars-Cov-2 et, si toujours symptomatique, le cinquième jour suivant la date du dernier contact rapproché avec des sujets confirmés positifs au Covid 19 Porter des appareils de protection respiratoire FFP2 pendant au moins 10 jours à compter de la dernière exposition au cas.\n \n Si vous ne présentez aucun symptôme et n'êtes pas vacciné ou n'avez pas terminé la primovaccination (vous n'avez reçu qu'une seule des deux doses de vaccin) ou si vous avez terminé la primo-vaccination depuis moins de 14 jours, ou si vous êtes asymptomatique et avez terminé la primo-vaccination ou vous êtes remis d'une précédente infection par le SRAS-CoV-2 depuis plus de 120 jours sans avoir reçu la dose de rappel, restez à maison pendant la durée de la quarantaine de 5 jours à compter du dernier contact avec le cas positif. Passé ce délai, vous devez effectuer un test d'antigène rapide ou moléculaire. Si le résultat est négatif, la quarantaine prend fin mais vous devez porter un équipement de protection FFP2 pendant les cinq prochains jours.\n \n Si pendant la période de quarantaine vous présentez des symptômes évocateurs d'une éventuelle infection au Sars-Cov-2, exécution immédiate d'un test de diagnostic.",
        "de" to "Für Sie ist keine Quarantäne vorgesehen und die Selbstüberwachungsmaßnahme für 5 Tage wird angewendet. \n \n Beim ersten Auftreten von Symptomen einen schnellen oder molekularen Antigentest zum Nachweis von Sars-Cov-2 durchführen und, falls noch symptomatisch, am fünften Tag nach dem Datum des letzten engen Kontakts mit positiv auf Covid 19 positiv getesteten Personen . Tragen Sie Geräte FFP2-Atemschutz für mindestens 10 Tage nach dem letzten Kontakt mit dem Fall. \n \n Wenn Sie keine Symptome haben und nicht geimpft sind oder die Grundimmunisierung nicht abgeschlossen haben (Sie haben nur eine der beiden Impfdosen erhalten) oder wenn Sie die Grundimmunisierung weniger als 14 Tage abgeschlossen haben oder asymptomatisch sind und die Grundimmunisierung abgeschlossen haben oder sich seit mehr als 120 Tagen ohne Auffrischimpfung von einer früheren SARS-CoV-2-Infektion erholt haben, bleiben Sie bei Haus für die Dauer der Quarantäne von 5 Tagen ab dem letzten Kontakt mit dem positiven Fall. Nach dieser Zeit müssen Sie einen schnellen oder molekularen Antigentest durchführen. Bei negativem Ergebnis endet die Quarantäne, aber Sie müssen für die nächsten fünf Tage FFP2-Schutzausrüstung tragen. \n \n Sollten während der Quarantänezeit Symptome auftreten, die auf eine mögliche Sars-Cov-2-Infektion hindeuten, wird ein sofortiger diagnostischer Test empfohlen."
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
    eudcc_expiration = eudccMap(),
    risk_exposure = risk_exposure()
)
