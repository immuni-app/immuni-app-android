package org.immuni.android.models.survey

typealias QuestionAnswers = List<Answer>
typealias SurveyAnswers = Map<QuestionId, QuestionAnswers>

sealed class Condition {
    fun isSatisfied(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ): Boolean = when (this) {
        is TrueCondition -> {
            true
        }
        is FalseCondition -> {
            false
        }
        is OrCondition -> {
            conditions.any { it.isSatisfied(healthState, triageProfile, surveyAnswers) }
        }
        is AndCondition -> {
            conditions.all { it.isSatisfied(healthState, triageProfile, surveyAnswers) }
        }
        is NotCondition -> {
            !condition.isSatisfied(healthState, triageProfile, surveyAnswers)
        }
        is SimpleCondition -> {
            surveyAnswers[questionId]?.let { answers ->
                val answerIndexes = answers.map { (it as SimpleAnswer).index }
                matchingIndexes.any { answerIndexes.contains(it) }
            } ?: false
        }
        is CompositeCondition -> {
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
        is TriageProfileCondition -> {
            matchingProfiles.contains(triageProfile)
        }
        is StatesContainCondition -> {
            matchingStates.intersect(healthState).isNotEmpty()
        }
    }
}

object TrueCondition : Condition()

object FalseCondition : Condition()

data class OrCondition(val conditions: List<Condition>) : Condition()

data class AndCondition(val conditions: List<Condition>) : Condition()

data class NotCondition(val condition: Condition) : Condition()

data class SimpleCondition(
    val questionId: QuestionId,
    val matchingIndexes: List<AnswerIndex>
) : Condition()

data class CompositeCondition(
    val questionId: QuestionId,
    val matchingComponentIndexes: List<List<AnswerIndex?>>
) : Condition()

data class TriageProfileCondition(val matchingProfiles: List<TriageProfileId?>) : Condition()

data class StatesContainCondition(val matchingStates: Set<HealthState>) : Condition()
