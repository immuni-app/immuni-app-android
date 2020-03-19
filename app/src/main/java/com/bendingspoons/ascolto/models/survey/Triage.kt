package com.bendingspoons.ascolto.models.survey

class Triage(private val mapping: Map<HealthStatus, Condition>) {
    fun check(healthStatus: HealthStatus, answers: SurveyAnswers): Boolean {
        val condition = mapping[healthStatus] ?: error("HealthStatus $healthStatus not handled")
        return condition.isSatisfied(answers)
    }
}

enum class HealthStatus(val id: String) {
    INFECTED("infected");

    companion object {
        fun fromId(id: String) = values().first { it.id == id }
    }
}
