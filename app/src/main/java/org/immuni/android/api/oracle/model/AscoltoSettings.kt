package org.immuni.android.api.oracle.model

import org.immuni.android.models.survey.raw.RawSurvey
import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.Survey

@JsonClass(generateAdapter = true)
class AscoltoSettings(
    @field:Json(name = "development_devices") val developmentDevices: List<String> = listOf(),
    @field:Json(name = "reminder_notification_title") val reminderNotificationTitle: String = "Compila il diario",
    @field:Json(name = "reminder_notification_message") val reminderNotificationMessage: String = "Ricordati di compilare il diario clinico di oggi",
    @field:Json(name = "privacy_url") val privacyPolicyUrl: String? = null,
    @field:Json(name = "tos_url") val termsOfServiceUrl: String? = null,
    @field:Json(name = "faq_url") val faqUrl: String? = null,
    @field:Json(name = "survey_json") val rawSurvey: RawSurvey? = null,
    @field:Json(name = "disable_survey_back") val disableSurveyBack: Boolean = false
) : OracleSettings() {
    @Transient private var _survey: Survey? = null
    val survey: Survey?
        get() {
            if (_survey == null) _survey = rawSurvey?.survey()
            return _survey
        }
}
