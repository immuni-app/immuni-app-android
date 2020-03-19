package com.bendingspoons.ascolto.models.survey

class Triage(private val mapping: Map<HealthStatus, Condition>) {
    fun check(healthStatus: HealthStatus, answers: SurveyAnswers): Boolean {
        val condition = mapping[healthStatus] ?: error("HealthStatus $healthStatus not handled")
        return condition.isSatisfied(answers)
    }
}


enum class HealthStatus {
    COVID_POSITIVE,
    SERIOUS_SYMPTOMS,
    HEALTHY_WITH_ESTABLISHED_CONTACT,
    MILD_SYMPTOMS_NO_ESTABLISHED_CONTACT,
    HEALTHY_NO_ESTABLISHED_CONTACT
}
