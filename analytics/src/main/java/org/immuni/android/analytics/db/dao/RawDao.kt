package org.immuni.android.analytics.db.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface RawDao {

    @RawQuery
    suspend fun checkpoint(
        supportSQLiteQuery: SupportSQLiteQuery = SimpleSQLiteQuery("pragma wal_checkpoint(full)")
    ): Int
}