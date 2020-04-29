package org.immuni.android.analytics.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PicoEventResponse(
    @field:Json(name = "delta") val delta: Int = 0,
    @field:Json(name = "last_event_timestamp") val last_event_timestamp: Double = 0.0
)