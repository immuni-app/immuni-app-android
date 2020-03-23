package org.ascolto.onlus.geocrowd19.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ascolto.onlus.geocrowd19.android.db.converter.DateConverter
import org.ascolto.onlus.geocrowd19.android.db.converter.GenderConverter
import org.ascolto.onlus.geocrowd19.android.db.dao.UserInfoDao
import org.ascolto.onlus.geocrowd19.android.db.entity.UserInfoEntity

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
    //abstract fun userInfoDao(): UserInfoDao
}
