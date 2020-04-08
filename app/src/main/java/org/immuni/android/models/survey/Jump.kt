package org.immuni.android.models.survey

sealed class JumpDestination
data class QuestionJumpDestination(val questionId: QuestionId) : JumpDestination()
object EndOfSurveyJumpDestination : JumpDestination()

data class JumpItem(val destination: JumpDestination, private val condition: Condition) {
    fun shouldJump(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = condition.isSatisfied(
        healthState = healthState,
        triageProfile = triageProfile,
        surveyAnswers = surveyAnswers
    )
}

data class Jump(private val jumps: List<JumpItem>) {
    fun jump(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = jumps.find {
        it.shouldJump(
            healthState = healthState,
            triageProfile = triageProfile,
            surveyAnswers = surveyAnswers
        )
    }?.destination
}