package it.ministerodellasalute.immuni.logic.user.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GreenCertificate(
    @field:Json(name = "expiredDate") val expiredDate: String,
    @field:Json(name = "base64") val base64: String
)
