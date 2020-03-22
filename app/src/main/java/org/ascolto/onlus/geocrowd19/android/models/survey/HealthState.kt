package org.ascolto.onlus.geocrowd19.android.models.survey

import org.ascolto.onlus.geocrowd19.android.models.survey.HealthStateUpdaterItemType.*

enum class HealthStateUpdaterItemType {
    ADD, REMOVE
}

data class HealthStateUpdaterItem(
    val state: HealthState,
    val type: HealthStateUpdaterItemType,
    val conditions: List<ConditionItem>
) {
    fun updatedState(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ): UserHealthState {
        val allConditionsSatisfied = conditions.all {
            it.isSatisfied(healthState, triageProfile, surveyAnswers)
        }
        return if (allConditionsSatisfied) {
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

data class HealthStateUpdater(val updaters: List<HealthStateUpdaterItem>) {
    fun updatedState(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = updaters.fold(healthState) { healthState, updater ->
        updater.updatedState(healthState, triageProfile, surveyAnswers)
    }
}
