package org.immuni.android.models.survey

sealed class JumpDestination
data class QuestionJumpDestination(val questionId: QuestionId) : JumpDestination()
object EndOfSurveyJumpDestination : JumpDestination()

/**
 * Contains a [JumpDestination] and a [Condition] that tells whether to jump to the destination or
 * not.
 */
data class JumpItem(val destination: JumpDestination, private val condition: Condition) {
    /**
     * Checks whether the [condition] for the jump is satisfied, based on the
     * user's current health state, triage profile and survey answers given so far.
     */
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
    /**
     * Returns the [JumpDestination] to go to after the user answers this question, based on the
     * user's current health state, triage profile and survey answers given so far.
     */
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
