package org.immuni.android.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// TODO
@JsonClass(generateAdapter = true)
data class UploadDeviceDataRequest(
    @field:Json(name = "todo") val todo: String
)