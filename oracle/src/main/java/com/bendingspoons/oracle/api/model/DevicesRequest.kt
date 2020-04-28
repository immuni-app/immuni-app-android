package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DevicesRequest(
    @field:Json(name = "unique_id") val uniqueId: String
)