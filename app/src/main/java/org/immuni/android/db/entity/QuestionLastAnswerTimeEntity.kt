package org.immuni.android.db.entity

import androidx.room.Entity

@Entity(tableName = "question_last_answer_time_table", primaryKeys = ["userId", "questionId"])
class QuestionLastAnswerTimeEntity(
    var userId: String = "",
    var questionId: String = "",
    var timestamp: Long = 0
)
