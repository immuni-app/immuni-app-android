package com.bendingspoons.pico.db.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.bendingspoons.pico.db.entity.PicoEventEntity

@Dao
internal interface RawDao {

    @RawQuery
    suspend fun checkpoint(
        supportSQLiteQuery: SupportSQLiteQuery = SimpleSQLiteQuery("pragma wal_checkpoint(full)")
    ): Int
}