package org.immuni.android.models.survey

import org.immuni.android.models.survey.HealthStateUpdaterItemType.*

enum class HealthStateUpdaterItemType {
    ADD, REMOVE
}

/**
 * Updates the user's current [HealthState] by adding or removing [state] to it, according to
 * [type], if the [condition] is satisfied.
 */
data class HealthStateUpdaterItem(
    val state: HealthState,
    val type: HealthStateUpdaterItemType,
    val condition: Condition
) {
    /**
     * Updates the user's current [HealthState] by adding or removing [state] to it, according to
     * [type], if the [condition] is satisfied based on the user's health state, triage profile, and
     * answers given so far.
     */
    fun updatedState(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ): UserHealthState {
        return if (condition.isSatisfied(healthState, triageProfile, surveyAnswers)) {
            healthState.toMutableSet().apply {
                when (type) {
                    ADD -> add(state)
                    REMOVE -> remove(state)
                }
            }
        } else {
            healthState
        }
    }
}

/**
 * Executes the [updaters] in order and folds their results in an updated [UserHealthState].
 */
data class HealthStateUpdater(val updaters: List<HealthStateUpdaterItem>) {
    /**
     * Executes the [updaters] in order and returns the updated [UserHealthState] created by folding
     * together their results.
     */
    fun updatedState(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = updaters.fold(healthState) { healthState, updater ->
        updater.updatedState(healthState, triageProfile, surveyAnswers)
    }
}
