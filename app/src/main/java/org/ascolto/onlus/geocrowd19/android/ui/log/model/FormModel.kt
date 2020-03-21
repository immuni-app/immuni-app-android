package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import java.io.Serializable

data class FormModel(
    var healthState: UserHealthState = setOf(),
    var triageProfile: TriageProfile? = null,
    var surveyAnswers: HashMap<QuestionId, QuestionAnswers> = hashMapOf(),
    var answeredQuestionsOrdered: MutableList<QuestionId> = mutableListOf()
): Serializable {
    private fun addQuestion(id: QuestionId) {
        val index = answeredQuestionsOrdered.indexOf(id)
        if(index != -1) {
            answeredQuestionsOrdered = answeredQuestionsOrdered.subList(0, index)
        }
        answeredQuestionsOrdered.add(id)
    }

    fun addAnswers(id: QuestionId, answers: QuestionAnswers) {
        addQuestion(id)

        for (answer in answers) {
            var questionAnswers = surveyAnswers[id]
            if (questionAnswers == null) {
                questionAnswers = listOf()
            }
            questionAnswers = questionAnswers.toMutableList().apply { add(answer) }
            surveyAnswers[id] = questionAnswers
        }
    }
}
