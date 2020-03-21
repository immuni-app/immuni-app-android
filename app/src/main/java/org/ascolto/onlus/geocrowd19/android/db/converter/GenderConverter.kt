package org.ascolto.onlus.geocrowd19.android.db.converter

import androidx.room.TypeConverter
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender

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
