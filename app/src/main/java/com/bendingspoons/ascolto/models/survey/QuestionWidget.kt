package com.bendingspoons.ascolto.models.survey

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
