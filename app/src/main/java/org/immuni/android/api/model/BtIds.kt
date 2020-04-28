package org.immuni.android.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BtIds(
    @field:Json(name = "ts") val serverTimestamp: Double,
    @field:Json(name = "ids") val ids: List<BtId> = listOf()
)

@JsonClass(generateAdapter = true)
data class BtId(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "ts") val expirationTimestamp: Double = -1.0
)
