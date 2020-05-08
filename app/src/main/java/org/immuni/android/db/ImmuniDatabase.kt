package org.immuni.android.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.io.File
import org.immuni.android.db.converter.DateConverter
import org.immuni.android.db.converter.GenderConverter
import org.immuni.android.db.dao.BLEContactDao
import org.immuni.android.db.dao.HealthProfileDao
import org.immuni.android.db.dao.QuestionLastAnswerTimeDao
import org.immuni.android.db.dao.RawDao
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.HealthProfileEntity
import org.immuni.android.db.entity.QuestionLastAnswerTimeEntity

const val DATABASE_VERSION = 9
const val DATABASE_NAME = "immuni_database"

@Database(
    entities = [
        BLEContactEntity::class,
        HealthProfileEntity::class,
        QuestionLastAnswerTimeEntity::class
    ],
    version = DATABASE_VERSION
)
@TypeConverters(
    DateConverter::class,
    GenderConverter::class
)
abstract class ImmuniDatabase : RoomDatabase() {
    abstract fun bleContactDao(): BLEContactDao
    abstract fun healthProfileDao(): HealthProfileDao
    abstract fun questionLastAnswerTimeDao(): QuestionLastAnswerTimeDao
    abstract fun rawDao(): RawDao

    companion object {
        fun databaseSize(context: Context): Long {
            val file: File = context.getDatabasePath(DATABASE_NAME)
            return file.length()
        }
    }
}
