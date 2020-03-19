package com.bendingspoons.ascolto.models.survey

import com.bendingspoons.ascolto.models.survey.ConditionPredicateType.*

typealias QuestionAnswers = List<Answer>
typealias SurveyAnswers = Map<QuestionId, QuestionAnswers>
typealias RawConditionPredicate = Map<String, Any>

enum class ConditionPredicateType(val id: String) {
    SIMPLE("one_dimensional"),
    COMPOSITE("two_dimensional");

    companion object {
        fun fromId(id: String) = values().first { it.id == id }
    }
}

sealed class ConditionPredicate

class SimpleConditionPredicate(
    val matchingIndexes: List<AnswerIndex>
) : ConditionPredicate()

class CompositeConditionPredicate(
    val matchingComponentIndexes: List<List<AnswerIndex?>>
): ConditionPredicate()

data class RawConditionItem(
    val questionId: QuestionId,
    val predicate: RawConditionPredicate
) {
    val conditionItem: ConditionItem

    init {
        val type = ConditionPredicateType.fromId(predicate["type"] as String)
        val predicate = when (type) {
            SIMPLE -> SimpleConditionPredicate(
                predicate["matching_indexes"] as List<AnswerIndex>
            )
            COMPOSITE -> CompositeConditionPredicate(
                predicate["matching_component_indexes"] as List<List<AnswerIndex>>
            )
        }
        conditionItem = ConditionItem(questionId, predicate)
    }
}

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
