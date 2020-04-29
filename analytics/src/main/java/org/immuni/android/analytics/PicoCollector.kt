package org.immuni.android.analytics

import android.util.Log
import kotlinx.coroutines.flow.collect

// PicoCollector receive the flow of events, set the request_timestamp to each events,
// try to dispatch the events to Pico server and if succeed delete them from the store.

internal class PicoCollector(
    private val flow: PicoFlow,
    private val dispatcher: PicoDispatcher,
    private val store: PicoStore
) {
    suspend fun start() {
        flow.flow().collect { list ->

            val events = list.map {
                it.apply {
                    requestTimestamp = System.currentTimeMillis() / 1000.0
                }
            }

            val response = dispatcher.dispatchEvents(events)
            if (response.isSuccessful) {
                store.deleteEvents(events)
            } else {
                // if the events are malformed delete it to avoid blocking future ones
                if(response.code() == 400 || response.code() == 422) {
                    store.deleteEvents(events)
                    Log.w("PICO", "Events deleted because malformed or invalid, error code ${response.code()}")
                }
            }
        }
    }
}
