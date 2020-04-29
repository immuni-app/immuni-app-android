package org.immuni.android.analytics.db.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import org.immuni.android.analytics.db.entity.PicoEventEntity

@Dao
internal interface PicoEventDao {

    @Query("SELECT * from pico_event_table LIMIT :max")
    suspend fun getPicoEvents(max: Int): List<PicoEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: PicoEventEntity)

    @Query("DELETE FROM pico_event_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(vararg events: PicoEventEntity)

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}