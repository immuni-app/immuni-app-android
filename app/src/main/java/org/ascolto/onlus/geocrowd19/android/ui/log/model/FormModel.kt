package org.ascolto.onlus.geocrowd19.android.ui.log.model

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import java.io.Serializable

data class FormModel(
    var currentQuestion: QuestionId,
    var healthState: UserHealthState,
    var triageProfile: TriageProfileId?,
    var surveyAnswers: LinkedHashMap<QuestionId, QuestionAnswers>
): Serializable {
    fun addAnswers(id: QuestionId, answers: QuestionAnswers) {
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
