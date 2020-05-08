package org.immuni.android.models.survey.raw

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.MultipleChoicesWidget
import org.immuni.android.models.survey.PickerWidget
import org.immuni.android.models.survey.RadioWidget

enum class RawQuestionWidgetType {
    @Json(name = "picker")
    PICKER,

    @Json(name = "multiple_choices")
    MULTIPLE_CHOICES,

    @Json(name = "radio")
    RADIO
}

@JsonClass(generateAdapter = true)
data class RawQuestionWidget(
    @field:Json(name = "type") val type: RawQuestionWidgetType,
    @field:Json(name = "components") val components: List<List<String>>? = null,
    @field:Json(name = "min_answers") val minNumberOfAnswers: Int? = null,
    @field:Json(name = "max_answers") val maxNumberOfAnswers: Int? = null,
    @field:Json(name = "answers") val answers: List<String>? = null
) {
    fun widget() = when (type) {
        RawQuestionWidgetType.PICKER -> {
            PickerWidget(
                components = components!!
            )
        }
        RawQuestionWidgetType.MULTIPLE_CHOICES -> {
            MultipleChoicesWidget(
                minNumberOfAnswers = minNumberOfAnswers ?: 0,
                maxNumberOfAnswers = maxNumberOfAnswers ?: 9999,
                answers = answers!!
            )
        }
        RawQuestionWidgetType.RADIO -> {
            RadioWidget(
                answers = answers!!
            )
        }
    }
}
