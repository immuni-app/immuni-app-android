package com.bendingspoons.pico

import com.bendingspoons.pico.api.PicoService
import com.bendingspoons.pico.model.PicoEvent
import com.bendingspoons.pico.api.model.PicoEventRequest
import com.bendingspoons.pico.api.model.PicoEventResponse
import retrofit2.Response

class PicoDispatcher(val api: PicoService) {

    internal var delta = 0
    internal var lastEventTimestamp = System.currentTimeMillis() / 1000.0

    // Dispatch a batch of PicoEvent to Pico.
    // Update the delta and lastEventTimestamp after each successful request.

    suspend fun dispatchEvents(events: List<PicoEvent>): Response<PicoEventResponse> {
        val result = api.event(
            PicoEventRequest(
                delta = delta,
                lastEventTimestamp = lastEventTimestamp,
                events = events
            )
        )

        if (result.isSuccessful) {
            result.body()?.let {
                delta = it.delta
                lastEventTimestamp = it.last_event_timestamp
            }
        }

        return result
    }
}