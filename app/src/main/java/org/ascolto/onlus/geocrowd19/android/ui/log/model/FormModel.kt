package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.QuestionAnswers
import org.ascolto.onlus.geocrowd19.android.models.survey.QuestionId
import org.ascolto.onlus.geocrowd19.android.models.survey.TriageProfileId
import org.ascolto.onlus.geocrowd19.android.models.survey.UserHealthState
import java.io.Serializable
import java.util.*

data class FormModel(
    val questionHistory: Stack<QuestionId>,
    var healthState: UserHealthState,
    var triageProfile: TriageProfileId?,
    val surveyAnswers: LinkedHashMap<QuestionId, QuestionAnswers>,
    val startDate: Date = Date()
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
