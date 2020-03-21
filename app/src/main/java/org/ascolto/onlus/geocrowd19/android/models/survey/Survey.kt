package org.ascolto.onlus.geocrowd19.android.models.survey

data class Survey(
    val version: String,
    val logicVersion: String,
    val questions: List<Question>,
    val triage: Triage
) {
    fun triage(triageStatus: TriageStatus?, answers: SurveyAnswers) =
        triage.triage(triageStatus, answers)
}

fun Survey.nextQuestion(questionId: String, answers: SurveyAnswers): Question? {
    val currentQuestion = questions.first { it.id == questionId }
    val currentPosition = questions.indexOf(currentQuestion)
    val nextQuestions = questions.takeLast(questions.size - currentPosition)

    if (currentQuestion.shouldStopSurvey(null, answers)) {
        return null
    }

    return nextQuestions.firstOrNull { it.shouldBeShown(null, answers) }
}
