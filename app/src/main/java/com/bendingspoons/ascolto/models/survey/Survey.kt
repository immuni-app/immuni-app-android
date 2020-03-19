package com.bendingspoons.ascolto.models.survey

data class Survey(
    val id: String,
    val questions: List<Question>,
    val triage: Triage
) {
    fun triage(healthStatus: HealthStatus, answers: SurveyAnswers) = triage.check(healthStatus, answers)
}
