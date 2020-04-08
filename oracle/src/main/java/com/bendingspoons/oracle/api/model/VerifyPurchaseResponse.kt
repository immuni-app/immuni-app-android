package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyPurchaseResponse(
    @field:Json(name = "settings") val settings: OracleSettings?
)