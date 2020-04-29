package org.immuni.android.analytics

import org.immuni.android.analytics.model.PicoEvent
import org.immuni.android.analytics.db.PicoDatabase
import org.immuni.android.analytics.db.entity.PicoEventEntity
import org.immuni.android.analytics.userconsent.UserConsent
import org.immuni.android.analytics.userconsent.UserConsentLevel

// PicoStore stores and loads events from the underlying Room database.

interface PicoStore {
    suspend fun store(event: PicoEvent)
    suspend fun nextEventsBatch(): List<PicoEvent>
    suspend fun deleteEvents(events: List<PicoEvent>)
}

internal class PicoStoreImpl(val database: PicoDatabase, val userConsent: UserConsent) :
    PicoStore {

    private val MAX_EVENTS_BATCH_SIZE = 10
    private val eventDao = database.picoEventDao()
    private val rawDao = database.rawDao()

    override suspend fun store(event: PicoEvent) {

        // user consent:
        // if denied do not store events,
        // if accepted store events,
        // if unkwnown store events.

        if(userConsent.level != UserConsentLevel.DENIED) {
            eventDao.insert(PicoEventEntity(event.id, event))
        }
    }

    override suspend fun nextEventsBatch(): List<PicoEvent> {
        return eventDao.getPicoEvents(MAX_EVENTS_BATCH_SIZE).map { it.event }
    }

    override suspend fun deleteEvents(events: List<PicoEvent>) {
        eventDao.delete(*events.map { PicoEventEntity(it.id, it) }.toTypedArray())
        rawDao.checkpoint()
    }
}
