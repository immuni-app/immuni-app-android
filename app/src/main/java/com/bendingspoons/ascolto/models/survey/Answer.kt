package com.bendingspoons.ascolto.models.survey

//class QuestionAnswer(
//    val questionId: QuestionId,
//    val answers: List<Answer>
//)

sealed class Answer

class SimpleAnswer(val index: AnswerIndex) : Answer()

class CompositeAnswer(val componentIndexes: List<AnswerIndex>) : Answer()
