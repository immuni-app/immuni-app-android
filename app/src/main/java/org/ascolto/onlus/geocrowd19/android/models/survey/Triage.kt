package org.ascolto.onlus.geocrowd19.android.models.survey

import org.ascolto.onlus.geocrowd19.android.R

typealias HealthState = String
typealias UserHealthState = Set<HealthState>

data class Triage(
    val profiles: List<TriageProfile>,
    val conditions: List<TriageCondition>
) {
    fun triage(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ): TriageProfile? {
        val profileId = conditions.find {
            it.check(healthState, triageProfile, answers)
        }?.profileId
        return profileId?.let { profiles.first { it.id == profileId } }
    }

    fun profile(id: TriageProfileId) = profiles.find { it.id == id }
}

typealias TriageProfileId = String

data class TriageProfile(
    val id: TriageProfileId,
    val url: String,
    val severity: Severity
)

enum class Severity {
    LOW, MID, HIGH
}

data class TriageCondition(
    val profileId: TriageProfileId,
    val condition: Condition
) {
    fun check(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ): Boolean {
        return condition.isSatisfied(healthState, triageProfile, answers)
    }
}

fun Severity.backgroundColor(): Int {
    return when(this) {
        Severity.LOW -> R.color.home_background
        Severity.MID -> R.color.card_yellow_bg
        Severity.HIGH -> R.color.card_red_bg
    }
}
