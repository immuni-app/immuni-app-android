package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.MultipleChoicesWidget
import com.bendingspoons.ascolto.models.survey.PickerWidget
import com.bendingspoons.ascolto.models.survey.RadioWidget
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class RawWidgetType {
    @Json(name = "picker")
    PICKER,

    @Json(name = "multiple_choices")
    MULTIPLE_CHOICES,

    @Json(name = "radio")
    RADIO
}

@JsonClass(generateAdapter = true)
data class RawWidget(
    @field:Json(name = "type") val type: RawWidgetType,
    @field:Json(name = "components") val components: List<List<String>>? = null,
    @field:Json(name = "min_number_of_answers") val minNumberOfAnswers: Int? = null,
    @field:Json(name = "max_number_of_answers") val maxNumberOfAnswers: Int? = null,
    @field:Json(name = "answers") val answers: List<String>? = null
) {
    fun widget() = when (type) {
        RawWidgetType.PICKER -> {
            PickerWidget(
                components = components!!
            )
        }
        RawWidgetType.MULTIPLE_CHOICES -> {
            MultipleChoicesWidget(
                minNumberOfAnswers = minNumberOfAnswers!!,
                maxNumberOfAnswers = maxNumberOfAnswers!!,
                answers = answers!!
            )
        }
        RawWidgetType.RADIO -> {
            RadioWidget(
                answers = answers!!
            )
        }
    }
}
