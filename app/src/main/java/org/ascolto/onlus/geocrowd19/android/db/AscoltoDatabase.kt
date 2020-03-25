package org.ascolto.onlus.geocrowd19.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ascolto.onlus.geocrowd19.android.db.converter.DateConverter
import org.ascolto.onlus.geocrowd19.android.db.converter.GenderConverter
import org.ascolto.onlus.geocrowd19.android.db.dao.BLEContactDao
import org.ascolto.onlus.geocrowd19.android.db.dao.UserInfoDao
import org.ascolto.onlus.geocrowd19.android.db.entity.BLEContactEntity
import org.ascolto.onlus.geocrowd19.android.db.entity.UserInfoEntity

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
