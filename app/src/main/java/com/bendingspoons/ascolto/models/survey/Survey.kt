package com.bendingspoons.ascolto.models.survey

import com.squareup.moshi.JsonClass

data class Survey(
    val version: String,
    val questions: List<Question>,
    val triage: Triage
) {
    fun triage(healthStatus: HealthStatus, answers: SurveyAnswers) = triage.check(healthStatus, answers)
}
