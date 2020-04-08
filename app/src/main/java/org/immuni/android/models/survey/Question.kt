package org.immuni.android.models.survey

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