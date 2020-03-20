package com.bendingspoons.ascolto.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bendingspoons.ascolto.db.AscoltoDatabase
import java.util.*
import kotlin.math.roundToInt

@Entity(tableName = "user_info_table", primaryKeys = ["id"])
data class UserInfoEntity(
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var isMainUser: Boolean = false,
    var name: String = "",
    var birthDate: Date = Date(),
    var gender: Gender = Gender.FEMALE,
    var liveWithYou: Boolean = false,
    @Ignore
    var familyMembers: List<UserInfoEntity> = listOf()
) {
    suspend fun fillWithRelations(db: AscoltoDatabase) {
        familyMembers = db.userInfoDao().getFamilyMembersUserInfo()
    }
}
