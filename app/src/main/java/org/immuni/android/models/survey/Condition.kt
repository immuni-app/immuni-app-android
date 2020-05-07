package org.immuni.android.models.survey

typealias QuestionAnswers = List<Answer>
typealias SurveyAnswers = Map<QuestionId, QuestionAnswers>

/**
 * This class implements the logic for checking whether a survey condition is satisfied, given a
 * health state, a triage profile id, and the answers given to the survey so far.
 *
 * Its concrete subclasses are [TrueCondition], [FalseCondition], [OrCondition], [AndCondition],
 * [NotCondition], [SimpleCondition], [CompositeCondition], [TriageProfileCondition],
 * [StatesContainCondition].
 */
sealed class Condition {
    /**
     * Implements the logic
     */
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

/**
 * Condition which is always satisfied.
 */
object TrueCondition : Condition()

/**
 * Condition which is never satisfied.
 */
object FalseCondition : Condition()

/**
 * Condition which is satisfied when one of the passed-in conditions are satisfied.
 */
data class OrCondition(val conditions: List<Condition>) : Condition()

/**
 * Condition which is satisfied when all of the passed-in conditions are satisfied.
 */
data class AndCondition(val conditions: List<Condition>) : Condition()

/**
 * Condition which is satisfied when the passed-in condition is not satisfied.
 */
data class NotCondition(val condition: Condition) : Condition()

/**
 * Condition which is satisfied when any of the passed-in answer indexes for the specified question
 * matches any of the answer indexes chosen by the user.
 */
data class SimpleCondition(
    val questionId: QuestionId,
    val matchingIndexes: List<AnswerIndex>
) : Condition()

/**
 * Condition which is satisfied when any of the passed-in answer component index list matches
 * the answer component index list chosen by the user.
 */
data class CompositeCondition(
    val questionId: QuestionId,
    val matchingComponentIndexes: List<List<AnswerIndex?>>
) : Condition()

/**
 * Condition which is satisfied when the user's triage profile is contained in the passed-in triage
 * profiles.
 */
data class TriageProfileCondition(val matchingProfiles: List<TriageProfileId?>) : Condition()

/**
 * Condition which is satisfied when the intersection of the user's [HealthState]s and the passed-in
 * HealthStates is non empty.
 */
data class StatesContainCondition(val matchingStates: Set<HealthState>) : Condition()
