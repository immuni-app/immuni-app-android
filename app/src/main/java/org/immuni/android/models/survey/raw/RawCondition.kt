package org.immuni.android.models.survey.raw

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.*
import org.immuni.android.models.survey.raw.RawConditionType.*

enum class RawConditionType {
    @Json(name = "true")
    TRUE,
    @Json(name = "false")
    FALSE,
    @Json(name = "or")
    OR,
    @Json(name = "and")
    AND,
    @Json(name = "not")
    NOT,
    @Json(name = "simple")
    SIMPLE,
    @Json(name = "composite")
    COMPOSITE,
    @Json(name = "current_user_triage_profile")
    CURRENT_USER_TRIAGE_PROFILE,
    @Json(name = "states_contain")
    STATES_CONTAIN
}

@JsonClass(generateAdapter = true)
data class RawCondition(
    @field:Json(name = "type")
    val type: RawConditionType,
    @field:Json(name = "question_id")
    val questionId: QuestionId? = null,
    @field:Json(name = "matching_indexes")
    val matchingIndexes: List<AnswerIndex>? = null,
    @field:Json(name = "matching_component_indexes")
    val matchingComponentIndexes: List<List<AnswerIndex?>>? = null,
    @field:Json(name = "matching_profiles")
    val matchingProfiles: List<TriageProfileId?>? = null,
    @field:Json(name = "states")
    val matchingStates: List<HealthState>? = null,
    @field:Json(name = "conditions")
    val conditions: List<RawCondition>? = null,
    @field:Json(name = "condition")
    val condition: RawCondition? = null
) {
    fun condition(): Condition = when (type) {
        TRUE -> TrueCondition
        FALSE -> FalseCondition
        OR -> OrCondition(conditions!!.map { it.condition() })
        AND -> AndCondition(conditions!!.map { it.condition() })
        NOT -> NotCondition(condition!!.condition())
        SIMPLE -> SimpleCondition(
            questionId!!,
            matchingIndexes!!
        )
        COMPOSITE -> CompositeCondition(
            questionId!!,
            matchingComponentIndexes!!
        )
        CURRENT_USER_TRIAGE_PROFILE -> TriageProfileCondition(
            matchingProfiles!!
        )
        STATES_CONTAIN -> StatesContainCondition(
            matchingStates!!.toSet()
        )
    }
}
