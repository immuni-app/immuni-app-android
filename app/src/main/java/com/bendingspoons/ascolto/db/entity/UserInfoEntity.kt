package com.bendingspoons.ascolto.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import com.bendingspoons.ascolto.db.AscoltoDatabase
import java.util.*
import kotlin.math.roundToInt

@Entity(tableName = "user_info_table", primaryKeys = ["name"])
data class UserInfoEntity(
    var id: String = "",
    var isMainUser: Boolean = false,
    var name: String = "",
    var birthDate: Date = Date(),
    var gender: Gender = Gender.FEMALE,
    @Ignore
    var familyMembers: List<UserInfoEntity> = listOf()
) {
    suspend fun fillWithRelations(db: AscoltoDatabase) {
        familyMembers = db.userInfoDao().getFamilyMembersUserInfo()
    }
}
