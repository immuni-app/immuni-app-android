package org.immuni.android.models.survey

import java.io.Serializable

sealed class Answer: Serializable

class SimpleAnswer(val index: AnswerIndex) : Answer()

class CompositeAnswer(val componentIndexes: List<AnswerIndex>) : Answer()
