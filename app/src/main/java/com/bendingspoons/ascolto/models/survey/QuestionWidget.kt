package com.bendingspoons.ascolto.models.survey


enum class WidgetType(val id: String) {
    PICKER("picker"),
    MULTIPLE_CHOICES("multiple_choices"),
    RADIO("radio");

    companion object {
        fun fromId(id: String): WidgetType = values().first { it.id == id }
    }
}

sealed class QuestionWidget

data class PickerWidget(
    val components: List<List<String>>
) : QuestionWidget()

data class MultipleChoicesWidget(
    val minNumberOfAnswers: Int,
    val maxNumberOfAnswers: Int,
    val answers: List<String>
) : QuestionWidget()

data class RadioWidget(
    val answers: List<String>
) : QuestionWidget()
