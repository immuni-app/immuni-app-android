package com.bendingspoons.ascolto.db.converter

import androidx.room.TypeConverter
import com.bendingspoons.ascolto.db.entity.Gender

class GenderConverter {
    @TypeConverter
    fun serialize(value: Gender): String {
        return value.id
    }

    @TypeConverter
    fun deserialize(value: String): Gender {
        return Gender.fromId(value)
    }
}
