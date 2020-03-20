package com.bendingspoons.ascolto.models.survey

data class Triage(
    val statuses: List<TriageStatus>,
    val conditions: List<TriageCondition>
) {
    fun triage(lastStatus: TriageStatus?, answers: SurveyAnswers): TriageStatus? {
        return conditions.firstOrNull { it.check(lastStatus, answers) }?.status
    }
}

typealias TriageStatusId = String

data class TriageStatus(
    val id: TriageStatusId,
    val url: String,
    val severity: Severity
)

enum class Severity {
    LOW, MID, HIGH;

    val value = values().indexOf(this)
}

data class TriageCondition(
    val status: TriageStatus,
    val condition: Condition
) {
    fun check(triageStatus: TriageStatus?, answers: SurveyAnswers): Boolean {
        return condition.isSatisfied(triageStatus, answers)
    }
}
