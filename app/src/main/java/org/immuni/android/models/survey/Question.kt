package org.immuni.android.models.survey

typealias QuestionId = String
typealias AnswerIndex = Int

/**
 * Survey question shown to the user, and to which the user can answer.
 *
 * @param id the identifier of the question.
 * @param title the title of the question.
 * @param description the description of the question.
 * @param widget the kind of answer the user can give.
 * @param periodicity min number of days that need to pass between two showings of this question.
 * @param showCondition the condition that determines whether to show this question.
 * @param healthStateUpdater the object which updates the user's health state in response to their
 * answers to this question.
 * @param jump the eventual jump to do based on the user's health state, triage profile and answers.
 */
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
    /**
     * Checks whether this question should be shown based on how many days have elapsed since the
     * last time the user answered this question, and on whether the user's health state,
     * triage profile and answers given satisfy [showCondition].
     */
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

    /**
     * Returns the updated health state after applying the [healthStateUpdater] to the user's
     * current health state, triage profile and survey answers given so far.
     */
    fun updatedHealthState(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        surveyAnswers: SurveyAnswers
    ) = healthStateUpdater.updatedState(
        healthState = healthState,
        triageProfile = triageProfile,
        surveyAnswers = surveyAnswers
    )

    /**
     * Returns the [JumpDestination] to go to after the user answers this question, based on the
     * user's current health state, triage profile and survey answers given so far.
     */
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

/**
 * Produces a String concatenating all the answers given to this question with ", " as separator.
 */
fun Question.humanReadableAnswers(qa: QuestionAnswers): String {
    val builder = StringBuilder()
    qa.forEach { answer ->
        when (answer) {
            is SimpleAnswer -> {
                val index = answer.index
                val str = when (widget) {
                    is MultipleChoicesWidget -> {
                        widget.answers[index]
                    }
                    is RadioWidget -> {
                        widget.answers[index]
                    }
                    is PickerWidget -> {
                        error("SimpleAnswer can't have a widget of type PickerWidget")
                    }
                }
                if (builder.isNotEmpty()) builder.append(", ")
                builder.append(str)
            }
            is CompositeAnswer -> {
                val indices = answer.componentIndexes
                when (widget) {
                    is PickerWidget -> {
                        for (i in widget.components.indices) {
                            builder.append(widget.components[i][indices[i]])
                        }
                    }
                    else -> {
                        error("CompositeAnswer can't have a widget of a type other than PickerWidget")
                    }
                }
            }
        }
    }
    return builder.toString()
}
