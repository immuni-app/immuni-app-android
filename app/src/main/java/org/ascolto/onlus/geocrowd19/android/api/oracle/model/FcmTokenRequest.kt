package org.ascolto.onlus.geocrowd19.android.api.oracle.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FcmTokenRequest(
    @field:Json(name = "token") val token: String
)