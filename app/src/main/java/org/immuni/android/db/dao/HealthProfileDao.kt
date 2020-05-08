package org.immuni.android.db.dao

import androidx.room.*
import org.immuni.android.db.entity.HealthProfileEntity

@Dao
interface HealthProfileDao : BaseDao<HealthProfileEntity> {
    @Query("SELECT * FROM health_profile_table WHERE userId = :userId")
    suspend fun allHealthProfilesForUser(userId: String): List<HealthProfileEntity>

    suspend fun lastHealthProfileForUser(userId: String): HealthProfileEntity? = allHealthProfilesForUser(userId).lastOrNull()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(healthProfileEntity: HealthProfileEntity)

    @Delete
    override suspend fun delete(obj: HealthProfileEntity)

    @Query("DELETE FROM health_profile_table WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM health_profile_table WHERE surveyTimeMillis < :surveyTimeMillis")
    suspend fun deleteAllOlderThan(surveyTimeMillis: Long)
}
