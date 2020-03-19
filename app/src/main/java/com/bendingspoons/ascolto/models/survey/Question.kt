package com.bendingspoons.ascolto.models.survey

typealias QuestionId = String
typealias AnswerIndex = Int

data class Question(
    val id: QuestionId,
    val title: String,
    val description: String,
    val frequency: Int,
    val showCondition: Condition?,
    val stopSurveyCondition: ConditionItem?,
    val widget: QuestionWidget
) {
    fun shouldBeShown(surveyAnswers: SurveyAnswers) =
        showCondition?.isSatisfied(surveyAnswers) ?: true

    fun shouldStopSurvey(answers: QuestionAnswers) =
        stopSurveyCondition?.isSatisfied(answers) ?: false
}
