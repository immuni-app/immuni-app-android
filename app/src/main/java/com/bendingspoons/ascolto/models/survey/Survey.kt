package com.bendingspoons.ascolto.models.survey

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
    val currentPosition = questions.map { it.id }.indexOf(questionId)
    val nextQuestions = questions.filterIndexed { index, question -> index > currentPosition }

    nextQuestions.forEach { nextQuestion ->
        if(nextQuestion.shouldBeShown(null, answers)) {
            if(nextQuestion.shouldStopSurvey(null, answers)) return null
            return  nextQuestion
        }
    }

    return null
}
