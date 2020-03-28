package org.ascolto.onlus.geocrowd19.android.db.dao

import androidx.room.Dao
import androidx.room.Query
import org.ascolto.onlus.geocrowd19.android.db.entity.BLEContactEntity

@Dao
interface BLEContactDao: BaseDao<BLEContactEntity> {
    @Query("SELECT * FROM ble_contact_table")
    suspend fun getAll(): List<BLEContactEntity>

    @Query("SELECT COUNT(DISTINCT btId) FROM ble_contact_table")
    suspend fun getAllBtIdsCount(): Int

    @Query("SELECT DISTINCT btId FROM ble_contact_table")
    suspend fun getAllDistinctBtIds(): List<String>
}
