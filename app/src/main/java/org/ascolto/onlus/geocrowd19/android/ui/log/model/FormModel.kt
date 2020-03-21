package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.Answer
import java.io.Serializable

data class FormModel(
    var answers: HashMap<String, Answer> = hashMapOf(),
    var answeredQuestionsOrdered: MutableList<String> = mutableListOf()): Serializable {

    fun addQuestion(id: String) {
        val index = answeredQuestionsOrdered.indexOf(id)
        if(index != -1) {
            answeredQuestionsOrdered = answeredQuestionsOrdered.subList(0, index)
        }
        answeredQuestionsOrdered.add(id)
    }

    fun addAnswer(id: String, answer: Answer) {
        answers[id] = answer
    }
}