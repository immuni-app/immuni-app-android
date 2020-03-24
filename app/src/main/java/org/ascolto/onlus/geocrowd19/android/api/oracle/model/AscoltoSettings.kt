package org.ascolto.onlus.geocrowd19.android.api.oracle.model

import org.ascolto.onlus.geocrowd19.android.models.survey.raw.RawSurvey
import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import testSurveyJson

@JsonClass(generateAdapter = true)
class AscoltoSettings(
    // app specific properties
    @field:Json(name = "development_devices") val developmentDevices: List<String> = listOf(),
    @field:Json(name = "reminder_notification_title") val reminderNotificationTitle: String = "Compila il diario",
    @field:Json(name = "reminder_notification_message") val reminderNotificationMessage: String = "Ricordati di compilare il diario clinico di oggi",
    @field:Json(name = "privacy_url") val privacyPolicyUrl: String? = null,
    @field:Json(name = "faq_url") val faqUrl: String? = null,
    @field:Json(name = "survey_json") val _survey: RawSurvey? = null
) : OracleSettings() {
    val survey: Survey?
        get() = _survey?.survey()
}
