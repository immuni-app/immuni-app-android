package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

data class FormModel(
    var questionHistory: Stack<QuestionId>,
    var healthState: UserHealthState,
    var triageProfile: TriageProfileId?,
    var surveyAnswers: LinkedHashMap<QuestionId, QuestionAnswers>
): Serializable {
    val currentQuestion = questionHistory.peek()

    fun advanceTo(questionId: QuestionId) {
        questionHistory.push(questionId)
    }

    fun addAnswers(answers: QuestionAnswers) {
        for (answer in answers) {
            var questionAnswers = surveyAnswers[currentQuestion]
            if (questionAnswers == null) {
                questionAnswers = listOf()
            }
            questionAnswers = questionAnswers.toMutableList().apply { add(answer) }
            surveyAnswers[currentQuestion] = questionAnswers
        }
    }

    fun goBack() {
        if (questionHistory.isEmpty()) {
            return
        }
        surveyAnswers.remove(questionHistory.pop())
    }
}
