package com.bendingspoons.ascolto.models.survey

sealed class JumpDestination
class QuestionJumpDestination(val questionId: QuestionId) : JumpDestination()
class EndOfSurveyJumpDestination() : JumpDestination()

class JumpItem(val destination: JumpDestination, private val condition: Condition) {
    fun shouldJump(
        healthState: UserHealthState,
        triageProfile: TriageProfile?,
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
        triageProfile: TriageProfile?,
        surveyAnswers: SurveyAnswers
    ) = jumps.firstOrNull {
        it.shouldJump(
            healthState = healthState,
            triageProfile = triageProfile,
            surveyAnswers = surveyAnswers
        )
    }?.destination
}