package com.bendingspoons.ascolto.models.survey

import com.bendingspoons.ascolto.models.survey.WidgetType.*

typealias QuestionId = String
typealias AnswerIndex = Int

data class Question(
    val id: QuestionId,
    val title: String,
    val description: String,
    val frequency: Int,
    private val showConditions: List<RawConditionItem>? = null,
    private val stopSurveyCondition: RawConditionPredicate? = null,
    private val widgetInfo: Map<String, Any>
) {
    companion object {
        const val WIDGET_FIELD_TYPE = "type"
        const val WIDGET_FIELD_COMPONENTS = "components"
        const val WIDGET_FIELD_MIN_ANSWERS = "min_answers"
        const val WIDGET_FIELD_MAX_ANSWERS = "max_answers"
        const val WIDGET_FIELD_ANSWERS = "answers"
    }

    val widget: QuestionWidget

    val showCondition =
        if (showConditions != null) Condition(showConditions.map { it.conditionItem }) else null

    init {
        val type = WidgetType.fromId(widgetInfo[WIDGET_FIELD_TYPE] as String)
        widget = when (type) {
            PICKER -> {
                val components = widgetInfo[WIDGET_FIELD_COMPONENTS] as List<List<String>>
                PickerWidget(
                    components = components
                )
            }
            MULTIPLE_CHOICES -> {
                val minNumberOfAnswers = widgetInfo[WIDGET_FIELD_MIN_ANSWERS] as? Int ?: 0
                val maxNumberOfAnswers =
                    widgetInfo[WIDGET_FIELD_MAX_ANSWERS] as? Int ?: Int.MAX_VALUE
                val answers = widgetInfo[WIDGET_FIELD_ANSWERS] as List<String>
                MultipleChoicesWidget(
                    minNumberOfAnswers = minNumberOfAnswers,
                    maxNumberOfAnswers = maxNumberOfAnswers,
                    answers = answers
                )
            }
            RADIO -> {
                val answers = widgetInfo[WIDGET_FIELD_ANSWERS] as List<String>
                RadioWidget(
                    answers = answers
                )
            }
        }
    }

    fun shouldBeShown(surveyAnswers: SurveyAnswers) =
        showCondition?.isSatisfied(surveyAnswers) ?: true

    fun shouldStopSurvey(answers: QuestionAnswers) =
        stopSurveyCondition?.let {
            RawConditionItem(id, it)
        }?.conditionItem?.isSatisfied(answers) ?: false
}
