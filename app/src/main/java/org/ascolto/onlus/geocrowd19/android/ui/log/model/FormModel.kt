package org.ascolto.onlus.geocrowd19.android.ui.log.model

import com.bendingspoons.ascolto.models.survey.Answer
import com.bendingspoons.ascolto.models.survey.QuestionAnswers
import com.bendingspoons.ascolto.models.survey.QuestionId
import java.io.Serializable

data class FormModel(
    var answers: HashMap<String, QuestionAnswers> = hashMapOf(),
    var answeredQuestionsOrdered: MutableList<String> = mutableListOf()): Serializable {

    fun addQuestion(id: String) {
        val index = answeredQuestionsOrdered.indexOf(id)
        if(index != -1) {
            answeredQuestionsOrdered = answeredQuestionsOrdered.subList(0, index)
        }
        answeredQuestionsOrdered.add(id)
    }

    fun addAnswer(id: String, answer: Answer) {
        var questionAnswers = answers[id]
        if (questionAnswers == null) {
            questionAnswers = listOf()
        }
        questionAnswers = questionAnswers.toMutableList().apply { add(answer) }
        answers[id] = questionAnswers
    }
}
