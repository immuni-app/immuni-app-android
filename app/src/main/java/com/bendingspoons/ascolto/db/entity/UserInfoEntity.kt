package com.bendingspoons.ascolto.db.entity

import androidx.room.Entity
import java.util.*
import kotlin.math.roundToInt

@Entity(tableName = "user_info_table", primaryKeys = ["name"])
data class UserInfoEntity(
    var id: String = "",
    var name: String = "",
    var birthDate: Date = Date(),
    var gender: Gender = Gender.FEMALE,
    var familyMemberIds: List<String> = listOf()
)
