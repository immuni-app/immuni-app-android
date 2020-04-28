package org.immuni.android.models.survey

import java.io.Serializable

sealed class Answer: Serializable

data class SimpleAnswer(val index: AnswerIndex) : Answer()

data class CompositeAnswer(val componentIndexes: List<AnswerIndex>) : Answer()
