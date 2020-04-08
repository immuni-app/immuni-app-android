package com.bendingspoons.pico.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bendingspoons.pico.model.PicoEvent

@Entity(tableName = "pico_event_table")
class PicoEventEntity(
    @PrimaryKey
    var id: String,
    var event: PicoEvent
)