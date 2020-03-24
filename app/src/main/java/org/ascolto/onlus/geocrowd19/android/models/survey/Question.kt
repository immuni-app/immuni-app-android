package org.ascolto.onlus.geocrowd19.android.models.survey

typealias QuestionId = String
typealias AnswerIndex = Int

data class Question(
    val id: QuestionId,
    val title: String,
    val description: String,
    val widget: QuestionWidget,
    val periodicity: Int,
    val showCondition: Condition,
    val healthStateUpdater: HealthStateUpdater,
    val jump: Jump
) {
    fun shouldBeShown(
        daysSinceItWasLastAnswered: Int?,
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ): Boolean {
        val hasEnoughTimeElapsed = daysSinceItWasLastAnswered?.let { it >= periodicity } ?: true
        return hasEnoughTimeElapsed && showCondition.isSatisfied(
            healthState,
            triageProfile,
            surveyAnswers
        )
    }

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
