package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.Condition
import com.bendingspoons.ascolto.models.survey.Question
import com.bendingspoons.ascolto.models.survey.QuestionId
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawQuestion(
    @field:Json(name = "id") val id: QuestionId,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "description") val description: String,
    @field:Json(name = "frequency") val frequency: Int,
    @field:Json(name = "only_when") val showConditions: List<RawConditionItem>? = null,
    @field:Json(name = "stop_if") val stopSurveyCondition: RawConditionItem? = null,
    @field:Json(name = "widget") val widget: RawWidget
) {
    fun question() = Question(
        id = id,
        title = title,
        description = description,
        frequency = frequency,
        showCondition = showConditions?.let {
            Condition(it.map { item -> item.conditionItem() })
        },
        stopSurveyCondition = stopSurveyCondition?.conditionItem(),
        widget = widget.widget()
    )
}
