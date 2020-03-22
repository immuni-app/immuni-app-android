package org.ascolto.onlus.geocrowd19.android.ui.log.model

import android.util.Log
import org.ascolto.onlus.geocrowd19.android.models.survey.*
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

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
        Log.d("survey", "Advancing to: $currentQuestion $questionId")
    }

    fun saveAnswers(answers: QuestionAnswers) {
        Log.d("survey", "Saving answers for: $currentQuestion")
        surveyAnswers[currentQuestion] = answers
    }

    fun goBack() {
        if (questionHistory.isEmpty()) {
            return
        }
        surveyAnswers.remove(questionHistory.pop())
        Log.d("survey", "Going back to: $currentQuestion")
    }
}
