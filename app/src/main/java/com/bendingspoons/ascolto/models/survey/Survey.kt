package com.bendingspoons.ascolto.models.survey

data class Survey(
    val version: String,
    val logicVersion: String,
    val questions: List<Question>,
    val triage: Triage
) {
    fun triage(triageStatus: TriageStatus?, answers: SurveyAnswers) =
        triage.triage(triageStatus, answers)
}
