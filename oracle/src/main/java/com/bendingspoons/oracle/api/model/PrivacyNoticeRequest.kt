package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrivacyNoticeRequest(
    @field:Json(name = "version") val version: String,
    @field:Json(name = "consents") val consents: Map<String , Boolean>,
    @field:Json(name = "is_at_least_16") val isAtLeast16: String
)