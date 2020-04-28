package com.bendingspoons.pico

import com.bendingspoons.pico.model.PicoEvent
import com.bendingspoons.pico.userconsent.UserConsent
import com.bendingspoons.pico.userconsent.UserConsentLevel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// This is a Flow of list of PicoEvents.
// Every X seconds the flow try to get stored events and if they exist emit them.

internal class PicoFlow(private val store: PicoStore, val userConsent: UserConsent, val REPEAT: Int = Int.MAX_VALUE) {

    fun flow(): Flow<List<PicoEvent>> = flow {
        repeat(REPEAT) {

            withTimeoutOrNull(6000) {
                future.await()
                future = CompletableDeferred()
            }

            val storedEvents = store.nextEventsBatch()

            // user consent:
            // if denied remove all store events,
            // if accepted send events,
            // if unkwnown keep the events waiting for a choice.

            if(userConsent.level == UserConsentLevel.DENIED) {
                store.deleteEvents(storedEvents)
            } else if(userConsent.level == UserConsentLevel.ACCEPTED) {
                if (storedEvents.isNotEmpty()) emit(storedEvents)
            }
        }
    }

    internal var future = CompletableDeferred<Boolean>()

    fun flush() {
        future.complete(true)
    }
}