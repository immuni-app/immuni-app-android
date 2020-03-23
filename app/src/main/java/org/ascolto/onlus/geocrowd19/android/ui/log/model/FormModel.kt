package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import java.io.Serializable
import java.util.*

data class FormModel(
    private val initialQuestion: QuestionId,
    private val initialHealthState: Set<HealthState>,
    var triageProfile: TriageProfileId?,
    val surveyAnswers: LinkedHashMap<QuestionId, QuestionAnswers>,
    val startDate: Date = Date()
) : Serializable {
    val questionHistory = Stack<QuestionId>().apply {
        add(initialQuestion)
    }
    val healthStateHistory = Stack<UserHealthState>().apply {
        add(initialHealthState.toMutableSet())
    }

    val currentQuestion: QuestionId
        get() = questionHistory.peek()

    val healthState: UserHealthState
        get() = healthStateHistory.peek()

    fun advanceTo(questionId: QuestionId) {
        questionHistory.push(questionId)
        healthStateHistory.push(healthState.toMutableSet())
    }

    fun saveAnswers(answers: QuestionAnswers) {
        surveyAnswers[currentQuestion] = answers
    }

    fun saveHealthState(healthState: UserHealthState) {
        healthStateHistory.pop()
        healthStateHistory.push(healthState)
    }

    fun goBack() {
        if (questionHistory.isEmpty()) {
            return
        }
        healthStateHistory.pop()
        surveyAnswers.remove(questionHistory.pop())
    }
}
