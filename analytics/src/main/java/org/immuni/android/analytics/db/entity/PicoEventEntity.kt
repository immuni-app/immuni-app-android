package org.immuni.android.analytics.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.immuni.android.analytics.model.PicoEvent

@Entity(tableName = "pico_event_table")
class PicoEventEntity(
    @PrimaryKey
    var id: String,
    var event: PicoEvent
)