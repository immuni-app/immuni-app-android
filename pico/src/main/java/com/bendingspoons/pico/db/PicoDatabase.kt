package com.bendingspoons.pico.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.security.crypto.MasterKeys
import com.bendingspoons.pico.db.converter.PicoEventConverter
import com.bendingspoons.pico.db.dao.PicoEventDao
import com.bendingspoons.pico.db.dao.RawDao
import com.bendingspoons.pico.db.entity.PicoEventEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

const val DATABASE_VERSION = 1

@Database(
    entities = [PicoEventEntity::class],
    version = DATABASE_VERSION
)
@TypeConverters(
    PicoEventConverter::class
)
internal abstract class PicoDatabase : RoomDatabase() {
    abstract fun picoEventDao(): PicoEventDao
    abstract fun rawDao(): RawDao
}

internal fun picoDatabase(context: Context): PicoDatabase {
    val key = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val passphrase: ByteArray = SQLiteDatabase.getBytes(key.toCharArray())
    val factory = SupportFactory(passphrase)
    return Room.databaseBuilder(
        context,
        PicoDatabase::class.java,
        "pico_database"
    )
    .fallbackToDestructiveMigration()
    .openHelperFactory(factory)
    .build()
}
