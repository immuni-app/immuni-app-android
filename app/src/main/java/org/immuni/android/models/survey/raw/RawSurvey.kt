package org.immuni.android.models.survey.raw

import org.immuni.android.models.survey.Survey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawSurvey(
    @field:Json(name = "version") val version: String,
    @field:Json(name = "questions") val questions: List<RawQuestion>,
    @field:Json(name = "triage") val triage: RawTriage
) {
    fun survey() = Survey(
        version = version,
        questions = questions.map { it.question() },
        triage = triage.triage()
    )
}
