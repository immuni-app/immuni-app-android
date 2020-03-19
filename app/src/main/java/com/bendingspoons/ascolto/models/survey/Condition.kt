package com.bendingspoons.ascolto.models.survey

typealias QuestionAnswers = List<Answer>
typealias SurveyAnswers = Map<QuestionId, QuestionAnswers>

sealed class ConditionPredicate

class SimpleConditionPredicate(
    val matchingIndexes: List<AnswerIndex>
) : ConditionPredicate()

class CompositeConditionPredicate(
    val matchingComponentIndexes: List<List<AnswerIndex?>>
): ConditionPredicate()

data class ConditionItem(
    val questionId: QuestionId,
    val predicate: ConditionPredicate
) {
    fun isSatisfied(answers: QuestionAnswers): Boolean {
        return when (predicate) {
            is SimpleConditionPredicate -> {
                val answerIndexes = answers.map { (it as SimpleAnswer).index }
                predicate.matchingIndexes.any { answerIndexes.contains(it) }
            }
            is CompositeConditionPredicate -> {
                val answerComponentsIndexes = answers.map { (it as CompositeAnswer).componentIndexes }
                predicate.matchingComponentIndexes.any { matchingComponentIndexes ->
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
            }
        }
    }
}

data class Condition(
    val conditionItems: List<ConditionItem>
) {
    fun isSatisfied(surveyAnswers: SurveyAnswers): Boolean {
        return conditionItems.all { item ->
            surveyAnswers[item.questionId]?.let { answers ->
                item.isSatisfied(answers)
            } ?: false
        }
    }
}
