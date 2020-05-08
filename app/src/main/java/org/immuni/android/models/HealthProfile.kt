package org.immuni.android.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*
import org.immuni.android.models.survey.QuestionId
import org.immuni.android.models.survey.TriageProfileId
import org.immuni.android.models.survey.UserHealthState

@JsonClass(generateAdapter = true)
data class HealthProfile(
    @field:Json(name = "user_id") val userId: String,
    @field:Json(name = "health_state") val healthState: UserHealthState,
    @field:Json(name = "triage_profile_id") val triageProfileId: TriageProfileId?,
    @field:Json(name = "survey_version") val surveyVersion: String,
    @field:Json(name = "survey_time_millis") val surveyTimeMillis: Long,
    @field:Json(name = "survey_answers") val surveyAnswers: Map<QuestionId, Any>
) {
    val surveyDate: Date
        get() = Date(surveyTimeMillis)
}
