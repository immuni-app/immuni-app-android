package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerifyPurchase(
    @field:Json(name = "orderId") val orderId: String?,
    @field:Json(name = "packageName") val packageName: String?,
    @field:Json(name = "productId") val productId: String?,
    @field:Json(name = "purchaseTime") val purchaseTime: Long?,
    @field:Json(name = "purchaseToken") val purchaseToken: String?
)