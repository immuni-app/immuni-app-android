package org.immuni.android.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.immuni.android.db.entity.BLEContactEntity

@Dao
interface BLEContactDao: BaseDao<BLEContactEntity> {
    @Query("SELECT * FROM ble_contact_table")
    suspend fun getAll(): List<BLEContactEntity>

    @Query("SELECT * FROM ble_contact_table")
    fun getAllFlow(): Flow<List<BLEContactEntity>>

    @Query("SELECT COUNT(DISTINCT btId) FROM ble_contact_table")
    suspend fun getAllDistinctBtIdsCount(): Int

    @Query("SELECT * FROM ble_contact_table WHERE btId=:btId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByBtId(btId: String): BLEContactEntity?

    @Query("SELECT * FROM ble_contact_table WHERE timestamp > :start AND timestamp <= :end")
    suspend fun getAllBetweenTimestamps(start: Long, end: Long): List<BLEContactEntity>

    @Query("SELECT COUNT() FROM ble_contact_table")
    suspend fun getAllBtIdsCount(): Int

    @Query("SELECT DISTINCT btId FROM ble_contact_table")
    suspend fun getAllDistinctBtIds(): List<String>

    @Query("DELETE FROM ble_contact_table WHERE timestamp < :timestamp")
    suspend fun removeOlderThan(timestamp: Long)
}
