package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyPurchaseRequest(
    @field:Json(name = "purchases") val purchases: List<VerifyPurchase>
)