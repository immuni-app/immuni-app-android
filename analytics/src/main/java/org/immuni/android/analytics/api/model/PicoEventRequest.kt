package org.immuni.android.analytics.api.model

import org.immuni.android.analytics.model.PicoEvent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicoEventRequest(
    @field:Json(name = "delta") val delta: Int,
    @field:Json(name = "last_event_timestamp") val lastEventTimestamp: Double,
    @field:Json(name = "events") val events: List<PicoEvent>
)
