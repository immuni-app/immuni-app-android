package org.immuni.android.db.converter

import androidx.room.TypeConverter
import org.immuni.android.models.Gender

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
