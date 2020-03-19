package com.bendingspoons.ascolto.models.survey

import com.bendingspoons.ascolto.models.survey.raw.RawConditionItem
import com.bendingspoons.ascolto.models.survey.raw.RawQuestion
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawSurvey(
    @field:Json(name = "version") val version: String,
    @field:Json(name = "questions") val questions: List<RawQuestion>,
    @field:Json(name = "triage") val triage: Map<HealthStatus, List<RawConditionItem>>
) {
    fun survey() = Survey(
        version = version,
        questions = questions.map { it.question() },
        triage = Triage(
            triage.mapValues { Condition(it.value.map { item -> item.conditionItem() }) }
        )
    )
}
