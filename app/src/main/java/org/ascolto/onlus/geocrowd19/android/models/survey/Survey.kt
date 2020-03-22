package org.ascolto.onlus.geocrowd19.android.models.survey

data class Survey(
    val version: String,
    val questions: List<Question>,
    val triage: Triage
) {
    fun triage(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ) = triage.triage(healthState, triageProfile, answers)

    fun updatedHealthState(
        questionId: String,
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ) = question(questionId).updatedHealthState(
        healthState = healthState,
        triageProfile = triageProfile,
        surveyAnswers = answers
    )

    fun next(
        // FIXME: add check on frequency
        questionId: String,
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ): SurveyNextDestination {
        val currentQuestion = questions.first { it.id == questionId }
        val currentPosition = questions.indexOf(currentQuestion)
        val nextQuestions = questions.takeLast(questions.size - currentPosition - 1)

        val jumpDestination = currentQuestion.jump(
            healthState = healthState,
            triageProfile = triageProfile,
            surveyAnswers = answers
        )

        return when (jumpDestination) {
            is QuestionJumpDestination -> {
                SurveyQuestionDestination(questions.first { it.id == jumpDestination.questionId })
            }
            is EndOfSurveyJumpDestination -> {
                SurveyEndDestination()
            }
            null -> {
                nextQuestions.find {
                    it.shouldBeShown(healthState, triageProfile, answers)
                }?.let {
                    SurveyQuestionDestination(it)
                } ?: SurveyEndDestination()
            }
        }
    }

    fun question(id: QuestionId) = questions.first { it.id == id }
}

sealed class SurveyNextDestination

class SurveyQuestionDestination(val question: Question) : SurveyNextDestination()

class SurveyEndDestination : SurveyNextDestination()
