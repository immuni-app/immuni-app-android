package com.bendingspoons.ascolto.ui.log.model

import com.bendingspoons.ascolto.models.survey.Answer
import com.bendingspoons.ascolto.models.survey.QuestionId
import java.io.Serializable

data class FormModel(var answers: HashMap<QuestionId, Answer> = hashMapOf()): Serializable