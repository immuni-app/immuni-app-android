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
): ConditionItem()

class TriageStatusConditionItem(
    val matchingStatuses: List<TriageStatusId?>
): ConditionItem()

sealed class ConditionItem {
    fun isSatisfied(triageStatus: TriageStatus?, answers: QuestionAnswers): Boolean {
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
            is TriageStatusConditionItem -> {
                matchingStatuses.contains(triageStatus?.id)
            }
        }
    }
}

data class Condition(
    val conditionItems: List<ConditionItem>
) {
    fun isSatisfied(triageStatus: TriageStatus?, surveyAnswers: SurveyAnswers): Boolean {
        return conditionItems.all { item ->
            when (item) {
                is SimpleConditionItem -> surveyAnswers[item.questionId]?.let { answers ->
                    item.isSatisfied(triageStatus, answers)
                } ?: false
                is CompositeConditionItem -> surveyAnswers[item.questionId]?.let { answers ->
                    item.isSatisfied(triageStatus, answers)
                } ?: false
                is TriageStatusConditionItem -> item.isSatisfied(triageStatus, listOf())
            }
        }
    }
}
