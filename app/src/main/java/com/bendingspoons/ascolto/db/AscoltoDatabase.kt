package com.bendingspoons.ascolto.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bendingspoons.ascolto.db.converter.DateConverter
import com.bendingspoons.ascolto.db.converter.GenderConverter
import com.bendingspoons.ascolto.db.dao.UserInfoDao
import com.bendingspoons.ascolto.db.entity.UserInfoEntity

const val DATABASE_VERSION = 2

@Database(
    entities = [
        UserInfoEntity::class
    ],
    version = DATABASE_VERSION
)
@TypeConverters(
    DateConverter::class,
    GenderConverter::class
)
abstract class AscoltoDatabase : RoomDatabase() {
    abstract fun userInfoDao(): UserInfoDao
}
