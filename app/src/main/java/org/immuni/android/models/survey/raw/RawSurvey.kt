package org.immuni.android.models.survey.raw

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.Survey

/**
 * RawSurvey and all other Raw* types in this package, are intermediate models generated from the
 * deserialization of a survey json, and used to generate the corresponding non-raw models.
 */
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
