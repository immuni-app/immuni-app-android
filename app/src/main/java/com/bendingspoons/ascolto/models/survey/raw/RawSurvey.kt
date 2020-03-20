package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.Survey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawSurvey(
    @field:Json(name = "version") val version: String,
    @field:Json(name = "logic_version") val logicVersion: String,
    @field:Json(name = "questions") val questions: List<RawQuestion>,
    @field:Json(name = "triage") val triage: RawTriage
) {
    fun survey() = Survey(
        version = version,
        logicVersion = logicVersion,
        questions = questions.map { it.question() },
        triage = triage.triage()
    )
}
