package org.ascolto.onlus.geocrowd19.android.models.survey

data class Triage(
    val statuses: List<TriageStatus>,
    val conditions: List<TriageCondition>
) {
    fun triage(lastStatus: TriageStatus?, answers: SurveyAnswers): TriageStatus? {
        val statusId = conditions.firstOrNull { it.check(lastStatus, answers) }?.statusId
        return statusId?.let { statuses.first { it.id == statusId } }
    }
}

typealias TriageStatusId = String

data class TriageStatus(
    val id: TriageStatusId,
    val url: String,
    val severity: Severity
)

enum class Severity {
    LOW, MID, HIGH
}

data class TriageCondition(
    val statusId: TriageStatusId,
    val condition: Condition
) {
    fun check(triageStatus: TriageStatus?, answers: SurveyAnswers): Boolean {
        return condition.isSatisfied(triageStatus, answers)
    }
}
