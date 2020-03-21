package org.ascolto.onlus.geocrowd19.android.models.survey

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
    fun shouldBeShown(triageStatus: TriageStatus?, surveyAnswers: SurveyAnswers) =
        showCondition?.isSatisfied(triageStatus, surveyAnswers) ?: true

    fun shouldStopSurvey(triageStatus: TriageStatus?, answers: SurveyAnswers) =
        stopSurveyCondition?.isSatisfied(triageStatus, answers) ?: false
}
