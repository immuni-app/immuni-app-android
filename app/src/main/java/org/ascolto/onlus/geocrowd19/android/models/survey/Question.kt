package org.ascolto.onlus.geocrowd19.android.models.survey

typealias QuestionId = String
typealias AnswerIndex = Int

data class Question(
    val id: QuestionId,
    val title: String,
    val description: String,
    val widget: QuestionWidget,
    val frequency: Int,
    val showCondition: Condition,
    val healthStateUpdater: HealthStateUpdater,
    val jump: Jump
) {
    fun shouldBeShown(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = showCondition.isSatisfied(healthState, triageProfile, surveyAnswers)

    fun updatedHealthState(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = healthStateUpdater.updatedState(
        healthState = healthState,
        triageProfile = triageProfile,
        surveyAnswers = surveyAnswers
    )

    fun jump(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = jump.jump(
        healthState = healthState,
        triageProfile = triageProfile,
        surveyAnswers = surveyAnswers
    )
}
