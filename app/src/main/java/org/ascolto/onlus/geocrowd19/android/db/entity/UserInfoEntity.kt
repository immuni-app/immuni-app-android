package org.ascolto.onlus.geocrowd19.android.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import java.util.*

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
    //suspend fun fillWithRelations(db: AscoltoDatabase) {
        //familyMembers = db.userInfoDao().getFamilyMembersUserInfo()
    //}
}

fun UserInfoEntity.age(): Int {
    val birthdayCalendar = Calendar.getInstance().apply {
        time = birthDate
    }
    val today = Calendar.getInstance()
    return today[Calendar.YEAR] - birthdayCalendar[Calendar.YEAR]
}
