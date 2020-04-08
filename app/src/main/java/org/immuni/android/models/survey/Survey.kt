package org.immuni.android.models.survey

data class Survey(
    val version: String,
    private val questions: List<Question>,
    val triage: Triage
) {
    val questionCount: Int = questions.count()

    fun indexOfQuestion(id: QuestionId) = questions.indexOfFirst { it.id == id }

    fun question(id: QuestionId) = questions.first { it.id == id }

    fun questionAtIndex(index: Int) = questions[index]

    private fun _next(
        questionId: String?,
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers,
        answeredQuestionsElapsedDays: Map<QuestionId, Int>
    ): SurveyNextDestination {
        val currentQuestion = questionId?.let { id -> questions.first { it.id == id } }
        val currentPosition = currentQuestion?.let { questions.indexOf(currentQuestion) } ?: -1
        val nextQuestions = questions.takeLast(questionCount - currentPosition - 1)

        val jumpDestination = currentQuestion?.jump(
            healthState = healthState,
            triageProfile = triageProfile,
            surveyAnswers = answers
        )

        return when (jumpDestination) {
            is QuestionJumpDestination -> {
                SurveyQuestionDestination(questions.first { it.id == jumpDestination.questionId })
            }
            is EndOfSurveyJumpDestination -> {
                SurveyEndDestination
            }
            null -> {
                nextQuestions.find {
                    it.shouldBeShown(
                        answeredQuestionsElapsedDays[it.id],
                        healthState,
                        triageProfile,
                        answers
                    )
                }?.let {
                    SurveyQuestionDestination(it)
                } ?: SurveyEndDestination
            }
        }
    }

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
        questionId: String,
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers,
        answeredQuestionsElapsedDays: Map<QuestionId, Int>
    ) = _next(
        questionId = questionId,
        healthState = healthState,
        triageProfile = triageProfile,
        answers = answers,
        answeredQuestionsElapsedDays = answeredQuestionsElapsedDays
    )

    fun firstQuestionToShow(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answeredQuestionsElapsedDays: Map<QuestionId, Int>
    ): Question {
        val nextDestination = _next(
            questionId = null,
            healthState = healthState,
            triageProfile = triageProfile,
            answers = mapOf(),
            answeredQuestionsElapsedDays = answeredQuestionsElapsedDays
        )
        return (nextDestination as SurveyQuestionDestination).question
    }
}

sealed class SurveyNextDestination

class SurveyQuestionDestination(val question: Question) : SurveyNextDestination()

object SurveyEndDestination : SurveyNextDestination()
