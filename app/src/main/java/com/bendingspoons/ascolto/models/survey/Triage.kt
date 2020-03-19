package com.bendingspoons.ascolto.models.survey

data class HealthStatusTriage(
    val status: HealthStatus,
    val condition: Condition
) {
    fun check(healthStatus: HealthStatus, answers: SurveyAnswers): Boolean {
        return condition.isSatisfied(healthStatus, answers)
    }
}

data class Triage(private val statusTriages: List<HealthStatusTriage>) {
    fun triage(healthStatus: HealthStatus, answers: SurveyAnswers): HealthStatus {
        return statusTriages.firstOrNull { it.check(healthStatus, answers) }?.status
            ?: HealthStatus.HEALTHY_NO_ESTABLISHED_CONTACT
    }

    fun check(healthStatus: HealthStatus, answers: SurveyAnswers): Boolean {
        val statusTriage = statusTriages.first { it.status == healthStatus }
        return statusTriage.check(healthStatus, answers)
    }
}

enum class HealthStatus {
    COVID_POSITIVE,
    SERIOUS_SYMPTOMS,
    HEALTHY_WITH_ESTABLISHED_CONTACT,
    MILD_SYMPTOMS_NO_ESTABLISHED_CONTACT,
    HEALTHY_NO_ESTABLISHED_CONTACT
}
