package com.bendingspoons.pico.db.dao

import androidx.room.*
import com.bendingspoons.pico.db.entity.PicoEventEntity

@Dao
interface PicoEventDao {

    @Query("SELECT * from pico_event_table LIMIT :max")
    suspend fun getPicoEvents(max: Int): List<PicoEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: PicoEventEntity)

    @Query("DELETE FROM pico_event_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(vararg events: PicoEventEntity)
}