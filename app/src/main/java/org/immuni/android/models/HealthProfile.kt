package org.immuni.android.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.QuestionId
import org.immuni.android.models.survey.TriageProfileId
import org.immuni.android.models.survey.UserHealthState
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class HealthProfile(
    @field:Json(name = "user_id") val userId: String,
    @field:Json(name = "health_state") val healthState: UserHealthState,
    @field:Json(name = "triage_profile_id") val triageProfileId: TriageProfileId?,
    @field:Json(name = "survey_version") val surveyVersion: String,
    @field:Json(name = "survey_date") val surveyDate: Date,
    @field:Json(name = "survey_answers") val surveyAnswers: Map<QuestionId, Any>
) {
    val id = id(userId, surveyDate)

    companion object {
        private fun formattedSurveyDate(surveyDate: Date) =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(surveyDate)

        fun id(userId: String, surveyDate: Date): String {
            return "user_health_profile-${userId}-${formattedSurveyDate(surveyDate)}"
        }
    }
}

@JsonClass(generateAdapter = true)
data class HealthProfileIdList(
    @field:Json(name = "user_id") val userId: String,
    @field:Json(name = "profile_ids") val profileIds: List<String>
) {
    val id = id(userId)

    companion object {
        fun id(userId: String) = "user_health_profile_id_list_$userId"
    }
}
