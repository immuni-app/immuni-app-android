package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RawJumpItem(
    @field:Json(name = "to") val destination: String,
    @field:Json(name = "conditions") val conditions: List<RawConditionItem>
) {
    companion object {
        const val END_OF_SURVEY = "__end__"
    }

    fun jumpItem(): JumpItem {
        val destination =
            if (destination == END_OF_SURVEY) EndOfSurveyJumpDestination()
            else QuestionJumpDestination(destination)

        return JumpItem(
            destination = destination,
            condition = Condition(conditions.map { it.conditionItem() })
        )
    }
}