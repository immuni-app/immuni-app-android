package com.bendingspoons.pico

import com.bendingspoons.pico.model.PicoEvent
import com.bendingspoons.pico.db.PicoDatabase
import com.bendingspoons.pico.db.entity.PicoEventEntity

// PicoStore store and load events from the underlying Room database.

interface PicoStore {
    suspend fun store(event: PicoEvent)
    suspend fun nextEventsBatch(): List<PicoEvent>
    suspend fun deleteEvents(events: List<PicoEvent>)
}

class PicoStoreImpl(val database: PicoDatabase) : PicoStore {

    private val MAX_EVENTS_BATCH_SIZE = 10
    private val eventDao = database.picoEventDao()

    override suspend fun store(event: PicoEvent) {
        eventDao.insert(PicoEventEntity(event.id, event))
    }

    override suspend fun nextEventsBatch(): List<PicoEvent> {
        return eventDao.getPicoEvents(MAX_EVENTS_BATCH_SIZE).map { it.event }
    }

    override suspend fun deleteEvents(events: List<PicoEvent>) {
        eventDao.delete(*events.map { PicoEventEntity(it.id, it) }.toTypedArray())
    }
}
