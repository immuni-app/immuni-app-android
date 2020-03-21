package org.ascolto.onlus.geocrowd19.android.models.survey

typealias QuestionAnswers = List<Answer>
typealias SurveyAnswers = MutableMap<QuestionId, QuestionAnswers>

class SimpleConditionItem(
    val questionId: QuestionId,
    val matchingIndexes: List<AnswerIndex>
) : ConditionItem()

class CompositeConditionItem(
    val questionId: QuestionId,
    val matchingComponentIndexes: List<List<AnswerIndex?>>
) : ConditionItem()

class TriageProfileConditionItem(
    val matchingProfiles: List<TriageProfileId?>
) : ConditionItem()

class StatesContainConditionItem(
    val matchingStates: Set<HealthState>
) : ConditionItem()

class StatesDoNotContainConditionItem(
    val matchingStates: Set<HealthState>
) : ConditionItem()

sealed class ConditionItem {
    fun isSatisfied(
        healthState: UserHealthState,
        triageProfile: TriageProfile?,
        surveyAnswers: SurveyAnswers
    ) = when (this) {
        is SimpleConditionItem -> {
            surveyAnswers[questionId]?.let { answers ->
                val answerIndexes = answers.map { (it as SimpleAnswer).index }
                matchingIndexes.any { answerIndexes.contains(it) }
            } ?: false
        }
        is CompositeConditionItem -> {
            surveyAnswers[questionId]?.let { answers ->
                val answerComponentsIndexes = answers.map {
                    (it as CompositeAnswer).componentIndexes
                }
                matchingComponentIndexes.any { matchingComponentIndexes ->
                    val exactMatch = answerComponentsIndexes.contains(matchingComponentIndexes)
                    val wildcardAwareMatch = {
                        answerComponentsIndexes.any { componentIndexes ->
                            (componentIndexes zip matchingComponentIndexes).all { (componentIdex, matchingComponentIndex) ->
                                // null acts as a wildcard component
                                matchingComponentIndex == null || componentIdex == matchingComponentIndex
                            }
                        }
                    }
                    exactMatch || wildcardAwareMatch()
                }
            } ?: false
        }
        is TriageProfileConditionItem -> {
            matchingProfiles.contains(triageProfile?.id)
        }
        is StatesContainConditionItem -> {
            matchingStates.intersect(healthState).isNotEmpty()
        }
        is StatesDoNotContainConditionItem -> {
            matchingStates.intersect(healthState).isEmpty()
        }
    }
}

data class Condition(val conditions: List<ConditionItem>) {
    fun isSatisfied(
        healthState: UserHealthState,
        triageProfile: TriageProfile?,
        surveyAnswers: SurveyAnswers
    ) = conditions.all {
        it.isSatisfied(healthState, triageProfile, surveyAnswers)
    }
}
