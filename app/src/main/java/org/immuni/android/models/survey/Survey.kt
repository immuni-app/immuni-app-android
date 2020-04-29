package org.immuni.android.models.survey

/**
 * A survey that the user can compile, specifically designed for a clinical diary use case.
 * It has questions of different kinds, conditions that determine whether to show or not each
 * question based on previous answers as well as the current state, and the ability to triage
 * the user based on other conditions on their answers, state and previous triage profile.
 *
 * @property version the version of the survey.
 * @property questions the list of questions that comprise the survey.
 * @property triage see [Triage]
 */
data class Survey(
    val version: String,
    private val questions: List<Question>,
    val triage: Triage
) {
    /**
     * Returns the number of questions in the survey.
     */
    val questionCount: Int = questions.count()

    /**
     * Returns the index of the question with the specified id.
     */
    fun indexOfQuestion(id: QuestionId) = questions.indexOfFirst { it.id == id }

    /**
     * Returns the question with the specified id.
     */
    fun question(id: QuestionId) = questions.first { it.id == id }

    /**
     * Returns the question at the specified index.
     */
    fun questionAtIndex(index: Int) = questions[index]

    /**
     * Convenience method that forwards the call to the triage object.
     */
    fun triage(
        healthState: UserHealthState,
        triageProfile: TriageProfileId?,
        answers: SurveyAnswers
    ) = triage.triage(healthState, triageProfile, answers)

    /**
     * Convenience method that forwards the call to the question object with the specified id.
     */
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

    /**
     * Returns the next destination in the survey.
     *
     * @param questionId the id of the current question.
     * @param healthState the current [UserHealthState].
     * @param triageProfile the current [TriageProfileId].
     * @param answers the map of questionIds to answers given so far.
     * @param answeredQuestionsElapsedDays the map of questionIds to the number of days elapsed
     * since last answering the corresponding question.
     */
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

    /**
     * Returns the first question to show, based on current [UserHealthState], [TriageProfileId] and
     * the number of days elapsed since last answering each question.
     *
     * @param healthState the current [UserHealthState].
     * @param triageProfile the current [TriageProfileId].
     * @param answeredQuestionsElapsedDays the map of questionIds to the number of days elapsed
     * since last answering the corresponding question.
     */
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

/**
 * Models the next destination in the survey after a jump.
 * It can either be a question ([SurveyQuestionDestination]) or the end of the survey
 * ([SurveyEndDestination])
 */
sealed class SurveyNextDestination

/**
 * @see [SurveyNextDestination]
 *
 * @property question the question representing the next destination in the survey.
 */
class SurveyQuestionDestination(val question: Question) : SurveyNextDestination()

/**
 * @see [SurveyNextDestination]
 */
object SurveyEndDestination : SurveyNextDestination()
