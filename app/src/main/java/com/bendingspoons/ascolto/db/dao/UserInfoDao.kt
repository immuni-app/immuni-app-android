package com.bendingspoons.ascolto.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.bendingspoons.ascolto.db.entity.UserInfoEntity

@Dao
interface UserInfoDao: BaseDao<UserInfoEntity> {
    @Query("SELECT * FROM user_info_table WHERE isMainUser = 1 LIMIT 1")
    suspend fun getMainUserInfo(): UserInfoEntity?

    @Query("SELECT * FROM user_info_table WHERE isMainUser = 1 LIMIT 1")
    fun getMainUserInfoLiveData(): LiveData<UserInfoEntity?>

    @Query("SELECT * FROM user_info_table WHERE isMainUser = 0")
    suspend fun getFamilyMembersUserInfo(): List<UserInfoEntity>

    @Query("SELECT * FROM user_info_table WHERE isMainUser = 0")
    fun getFamilyMembersUserInfoLiveData(): LiveData<List<UserInfoEntity>>
}
