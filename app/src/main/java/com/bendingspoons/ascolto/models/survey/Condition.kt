package com.bendingspoons.ascolto.models.survey

typealias QuestionAnswers = List<Answer>
typealias SurveyAnswers = Map<QuestionId, QuestionAnswers>

class SimpleConditionItem(
    val questionId: QuestionId,
    val matchingIndexes: List<AnswerIndex>
) : ConditionItem()

class CompositeConditionItem(
    val questionId: QuestionId,
    val matchingComponentIndexes: List<List<AnswerIndex?>>
): ConditionItem()

class HealthStatusConditionItem(
    val matchingStatuses: List<HealthStatus>
): ConditionItem()

sealed class ConditionItem {
    fun isSatisfied(healthStatus: HealthStatus, answers: QuestionAnswers): Boolean {
        return when (this) {
            is SimpleConditionItem -> {
                val answerIndexes = answers.map { (it as SimpleAnswer).index }
                matchingIndexes.any { answerIndexes.contains(it) }
            }
            is CompositeConditionItem -> {
                val answerComponentsIndexes = answers.map {
                    (it as CompositeAnswer).componentIndexes
                }
                matchingComponentIndexes.any { matchingComponentIndexes ->
                    val exactMatch = answerComponentsIndexes.contains(matchingComponentIndexes)
                    val wildcardAwareMatch = {
                        answerComponentsIndexes.any { componentIndexes ->
                            (componentIndexes zip matchingComponentIndexes).all {
                                    (componentIdex, matchingComponentIndex) ->
                                // null acts as a wildcard component
                                matchingComponentIndex == null || componentIdex == matchingComponentIndex
                            }
                        }
                    }
                    exactMatch || wildcardAwareMatch()
                }
            }
            is HealthStatusConditionItem -> {
                matchingStatuses.contains(healthStatus)
            }
        }
    }
}

data class Condition(
    val conditionItems: List<ConditionItem>
) {
    fun isSatisfied(healthStatus: HealthStatus, surveyAnswers: SurveyAnswers): Boolean {
        return conditionItems.all { item ->
            when (item) {
                is SimpleConditionItem -> surveyAnswers[item.questionId]?.let { answers ->
                    item.isSatisfied(healthStatus, answers)
                } ?: false
                is CompositeConditionItem -> surveyAnswers[item.questionId]?.let { answers ->
                    item.isSatisfied(healthStatus, answers)
                } ?: false
                is HealthStatusConditionItem -> item.isSatisfied(healthStatus, listOf())
            }
        }
    }
}
