package org.immuni.android.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.immuni.android.db.AscoltoDatabase
import java.util.*

@Entity(tableName = "ble_contact_table")
data class BLEContactEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var timestamp: Double = Date().time / 1000.0,
    var signalStrength: Int = 0,
    var distanceInMeters: Double = 0.0,
    var btId: String
)