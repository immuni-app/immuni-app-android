package org.immuni.android.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "ble_contact_table")
data class BLEContactEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var timestamp: Date = Date(),
    var txPower: Int = 0,
    val rssi: Int = 0,
    var btId: String
)