package org.ascolto.onlus.geocrowd19.android.models.survey

sealed class JumpDestination
class QuestionJumpDestination(val questionId: QuestionId) : JumpDestination()
class EndOfSurveyJumpDestination() : JumpDestination()

class JumpItem(val destination: JumpDestination, private val condition: Condition) {
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

class Jump(private val jumps: List<JumpItem>) {
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