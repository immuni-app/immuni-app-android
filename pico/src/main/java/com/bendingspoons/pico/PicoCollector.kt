package com.bendingspoons.pico

import android.util.Log
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

// PicoCollector receive the flow of events, set the request_timestamp to each events,
// try to dispatch the events to Pico server and if succeed delete them from the store.

class PicoCollector(
    private val flow: PicoFlow,
    private val dispatcher: PicoDispatcher,
    private val store: PicoStore,
    private val config: PicoConfiguration
) {
    suspend fun start() {
        flow.flow().collect { list ->

            val events = list.map {
                it.apply {
                    requestTimestamp = System.currentTimeMillis() / 1000.0

                    // update the AAID in case it was not yet available during the event creation
                    // because the AAID retrieval is async
                    this.user.ids[Concierge.InternalId.AAID.keyName] = config.concierge().aaid.id
                    this.user.ids["idfa"] = config.concierge().aaid.id // legacy
                }
            }

            val response = dispatcher.dispatchEvents(events)
            if (response.isSuccessful) {
                store.deleteEvents(events)
            } else {
                // if the events are malformed delete it to avoid blocking the future ones
                if(response.code() == 400 || response.code() == 422) {
                    store.deleteEvents(events)
                    Log.w("PICO", "Events deleted because malformed or invalid, error code ${response.code()}")
                }
            }
        }
    }
}
