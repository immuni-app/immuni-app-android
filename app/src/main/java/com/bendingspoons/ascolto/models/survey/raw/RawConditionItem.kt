package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.*
import com.bendingspoons.ascolto.models.survey.raw.RawConditionType.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class RawConditionType {
    @Json(name = "simple")
    SIMPLE,
    @Json(name = "composite")
    COMPOSITE,
    @Json(name = "current_user_status")
    CURRENT_USER_STATUS
}

@JsonClass(generateAdapter = true)
data class RawConditionItem(
    @field:Json(name = "type")
    val type: RawConditionType,
    @field:Json(name = "question_id")
    val questionId: QuestionId?,
    @field:Json(name = "matching_indexes")
    val matchingIndexes: List<AnswerIndex>? = null,
    @field:Json(name = "matching_component_indexes")
    val matchingComponentIndexes: List<List<AnswerIndex>>? = null,
    @field:Json(name = "matching_statuses")
    val matchingStatuses: List<TriageStatusId?>? = null
) {
    fun conditionItem() = when (type) {
        SIMPLE -> SimpleConditionItem(
            questionId!!,
            matchingIndexes!!
        )
        COMPOSITE -> CompositeConditionItem(
            questionId!!,
            matchingComponentIndexes!!
        )
        CURRENT_USER_STATUS -> TriageStatusConditionItem(
            matchingStatuses!!
        )
    }
}
