package org.immuni.android.models.survey

import org.immuni.android.R

typealias HealthState = String
typealias UserHealthState = Set<HealthState>

/**
 * Triage contains the list of [TriageProfile]s and the list of [TriageCondition]s that map users'
 * answers, health state and previous profile to each triage profile.
 */
data class Triage(
    val profiles: List<TriageProfile>,
    val conditions: List<TriageCondition>
) {
    /**
     * Executes the triage of the health state of the user, based on their current health state,
     * their previous triage profile, and the answers given to the survey.
     */
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

    /**
     * Returns the [TriageProfile] with the specified id.
     */
    fun profile(id: TriageProfileId) = profiles.find { it.id == id }
}

typealias TriageProfileId = String

/**
 * The triage profile of a user who undertook a survey.
 *
 * @param id the identifier of this triage profile.
 * @param url the url of an informative page containing advice and directions on what to do.
 * @param severity the severity level of this triage profile.
 */
data class TriageProfile(
    val id: TriageProfileId,
    val url: String,
    val severity: Severity
)

/**
 * The severity level of a [TriageProfile]. Can be LOW, MID, or HIGH.
 */
enum class Severity {
    LOW, MID, HIGH
}

/**
 * Background color associated with each triage profile severity.
 */
fun Severity.backgroundColor(): Int {
    return when (this) {
        Severity.LOW -> R.color.home_background
        Severity.MID -> R.color.card_yellow_bg
        Severity.HIGH -> R.color.card_red_bg
    }
}

/**
 * It maps a user's answers, health state and previous profile to each triage profile.
 *
 * @param profileId the id of the triage profile this condition maps to.
 * @param condition the [Condition] to check.
 */
data class TriageCondition(
    val profileId: TriageProfileId,
    val condition: Condition
) {
    /**
     * Checks whether the provided [UserHealthState], [TriageProfileId] and survey answers satisfy
     * the condition.
     */
    fun check(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ): Boolean {
        return condition.isSatisfied(healthState, triageProfile, answers)
    }
}
