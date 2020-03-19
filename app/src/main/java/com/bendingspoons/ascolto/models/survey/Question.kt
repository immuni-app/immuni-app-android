package com.bendingspoons.ascolto.models.survey

typealias QuestionId = String
typealias AnswerIndex = Int

data class Question(
    val id: QuestionId,
    val title: String,
    val description: String,
    val frequency: Int,
    val showCondition: Condition?,
    val stopSurveyCondition: Condition?,
    val widget: QuestionWidget
) {
    fun shouldBeShown(healthStatus: HealthStatus, surveyAnswers: SurveyAnswers) =
        showCondition?.isSatisfied(healthStatus, surveyAnswers) ?: true

    fun shouldStopSurvey(healthStatus: HealthStatus, answers: SurveyAnswers) =
        stopSurveyCondition?.isSatisfied(healthStatus, answers) ?: false
}
