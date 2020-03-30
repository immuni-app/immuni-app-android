package org.immuni.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.immuni.android.db.converter.DateConverter
import org.immuni.android.db.converter.GenderConverter
import org.immuni.android.db.dao.BLEContactDao
import org.immuni.android.db.dao.UserInfoDao
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.UserInfoEntity

const val DATABASE_VERSION = 3

@Database(
    entities = [
        UserInfoEntity::class,
        BLEContactEntity::class
    ],
    version = DATABASE_VERSION
)
@TypeConverters(
    DateConverter::class,
    GenderConverter::class
)
abstract class AscoltoDatabase : RoomDatabase() {
    abstract fun bleContactDao(): BLEContactDao
}
