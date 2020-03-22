package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

data class FormModel(
    val questionHistory: Stack<QuestionId>,
    var healthState: UserHealthState,
    var triageProfile: TriageProfileId?,
    val surveyAnswers: LinkedHashMap<QuestionId, QuestionAnswers>
) : Serializable {
    val currentQuestion
        get() = questionHistory.peek()

    fun advanceTo(questionId: QuestionId) {
        questionHistory.push(questionId)
    }

    fun saveAnswers(answers: QuestionAnswers) {
        surveyAnswers[currentQuestion] = answers
    }

    fun goBack() {
        if (questionHistory.isEmpty()) {
            return
        }
        surveyAnswers.remove(questionHistory.pop())
    }
}
