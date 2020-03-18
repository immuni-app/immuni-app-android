package com.bendingspoons.ascolto.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bendingspoons.ascolto.db.converter.DateConverter
import com.bendingspoons.ascolto.db.entity.UserInfoEntity

const val DATABASE_VERSION = 1

@Database(
    entities = [
        UserInfoEntity::class],
    version = DATABASE_VERSION
)
@TypeConverters(
    DateConverter::class
)
abstract class AscoltoDatabase : RoomDatabase() {
}