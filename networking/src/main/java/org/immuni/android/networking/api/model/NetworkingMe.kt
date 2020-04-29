package org.immuni.android.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class NetworkingMe(
    @field:Json(name = "device_id") val deviceId: String = ""
)
