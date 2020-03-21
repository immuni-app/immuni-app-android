package org.ascolto.onlus.geocrowd19.android.models.survey.raw

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import org.ascolto.onlus.geocrowd19.android.models.survey.raw.RawConditionType.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class RawConditionType {
    @Json(name = "simple")
    SIMPLE,
    @Json(name = "composite")
    COMPOSITE,
    @Json(name = "current_user_triage_profile")
    CURRENT_USER_TRIAGE_PROFILE,
    @Json(name = "states_contain")
    STATES_CONTAIN,
    @Json(name = "states_not_contain")
    STATES_DO_NOT_CONTAIN
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
    @field:Json(name = "matching_profiles")
    val matchingProfiles: List<TriageProfileId?>? = null,
    @field:Json(name = "states")
    val matchingStates: List<HealthState>? = null
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
        CURRENT_USER_TRIAGE_PROFILE -> TriageProfileConditionItem(
            matchingProfiles!!
        )
        STATES_CONTAIN -> StatesContainConditionItem(
            matchingStates!!.toSet()
        )
        STATES_DO_NOT_CONTAIN -> StatesDoNotContainConditionItem(
            matchingStates!!.toSet()
        )
    }
}
