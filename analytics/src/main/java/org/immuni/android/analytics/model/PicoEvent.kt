package org.immuni.android.analytics.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicoEvent(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "timestamp") val timestamp: Double,
    @field:Json(name = "request_timestamp") var requestTimestamp: Double,
    @field:Json(name = "app") val app: String,
    @field:Json(name = "user") val user: PicoUser,
    @field:Json(name = "type") var type: String,
    @field:Json(name = "data") var data: Any?
)