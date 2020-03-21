package com.bendingspoons.ascolto.ui.log.model

import com.bendingspoons.ascolto.models.survey.Answer
import com.bendingspoons.ascolto.models.survey.QuestionId
import java.io.Serializable

data class FormModel(
    var answers: HashMap<String, Answer> = hashMapOf(),
    var questionsOrdered: MutableList<String> = mutableListOf()): Serializable {

    fun addQuestion(id: String) {
        val index = questionsOrdered.indexOf(id)
        if(index != -1) {
            questionsOrdered = questionsOrdered.subList(0, index)
        }
        questionsOrdered.add(id)
    }

    fun addAnswer(id: String, answer: Answer) {
        answers[id] = answer
    }
}