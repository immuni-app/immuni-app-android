package com.bendingspoons.ascolto.db.entity

import androidx.room.Entity
import java.util.*
import kotlin.math.roundToInt

@Entity(tableName = "user_info_table", primaryKeys = ["name"])
data class UserInfoEntity(
    var name: String = ""
)
