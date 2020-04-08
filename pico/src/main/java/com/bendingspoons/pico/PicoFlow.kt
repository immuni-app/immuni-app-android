package com.bendingspoons.pico

import com.bendingspoons.pico.model.PicoEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// This is a Flow of list of PicoEvents.
// Every X seconds the flow try to get stored events and if they exist emit them.

class PicoFlow(private val store: PicoStore, val REPEAT: Int = Int.MAX_VALUE) {

    fun flow(): Flow<List<PicoEvent>> = flow {
        repeat(REPEAT) {

            withTimeoutOrNull(6000) {
                future.await()
                future = CompletableDeferred()
            }

            val storedEvents = store.nextEventsBatch()
            if (storedEvents.isNotEmpty()) emit(storedEvents)
        }
    }

    internal var future = CompletableDeferred<Boolean>()

    fun flush() {
        future.complete(true)
    }
}