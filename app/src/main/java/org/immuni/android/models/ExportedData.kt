package org.immuni.android.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.QuestionId
import org.immuni.android.models.survey.SurveyAnswers
import java.util.*

@JsonClass(generateAdapter = true)
data class ExportData(
    @field:Json(name = "profile_id") val profileId: String,
    @field:Json(name = "devices") val devices: List<ExportDevice>
    //@field:Json(name = "surveys") val surveys: List<ExportHealthProfile>
)

@JsonClass(generateAdapter = true)
data class ExportDevice(
    @field:Json(name = "timestamp") val timestamp: Double,
    @field:Json(name = "bt_id") val btId: String,
    @field:Json(name = "events") val events: String
)

@JsonClass(generateAdapter = true)
data class ExportHealthProfile(
    @field:Json(name = "answers") val answers: Map<QuestionId, Any>,
    @field:Json(name = "calculated_triage_profile") val calculatedTriageProfile: String?,
    @field:Json(name = "profile_id") val profileId: String,
    @field:Json(name = "survey_version") val surveyVersion: String,
    @field:Json(name = "event_timestamp") val eventTimestamp: Double
) {
    companion object {
        fun fromHealthProfile(hp: HealthProfile): ExportHealthProfile {
            return ExportHealthProfile(
                answers = hp.surveyAnswers,
                calculatedTriageProfile = hp.triageProfileId,
                profileId = hp.userId,
                surveyVersion = hp.surveyVersion,
                eventTimestamp = hp.surveyDate.time / 1000.0
            )
        }
    }
}