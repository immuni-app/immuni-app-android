package org.immuni.android.db.dao

import androidx.room.Dao
import androidx.room.Query
import org.immuni.android.db.entity.QuestionLastAnswerTimeEntity

@Dao
interface QuestionLastAnswerTimeDao: BaseDao<QuestionLastAnswerTimeEntity> {
    @Query("SELECT * FROM question_last_answer_time_table WHERE userId = :userId")
    suspend fun allQuestionLastAnswerTimesForUser(userId: String): List<QuestionLastAnswerTimeEntity>

    @Query("DELETE FROM question_last_answer_time_table WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM question_last_answer_time_table WHERE timestamp < :timestamp")
    suspend fun deleteAllOlderThan(timestamp: Long)
}
