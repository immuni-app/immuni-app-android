package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawConditionPredicate(
    @field:Json(name = "type")
    val type: RawConditionPredicateType,
    @field:Json(name = "matching_indexes")
    val matchingIndexes: List<AnswerIndex>? = null,
    @field:Json(name = "matching_component_indexes")
    val matchingComponentIndexes: List<List<AnswerIndex>>? = null
) {
    fun conditionPredicate(): ConditionPredicate {
        return when (type) {
            RawConditionPredicateType.SIMPLE -> SimpleConditionPredicate(
                matchingIndexes!!
            )
            RawConditionPredicateType.COMPOSITE -> CompositeConditionPredicate(
                matchingComponentIndexes!!
            )
        }
    }
}

enum class RawConditionPredicateType {
    @Json(name = "one_dimensional")
    SIMPLE,
    @Json(name = "two_dimensional")
    COMPOSITE
}

@JsonClass(generateAdapter = true)
data class RawConditionItem(
    @field:Json(name = "question_id") val questionId: QuestionId,
    @field:Json(name = "answers_predicate") val predicate: RawConditionPredicate
) {
    fun conditionItem() = ConditionItem(
        questionId,
        predicate.conditionPredicate()
    )
}
