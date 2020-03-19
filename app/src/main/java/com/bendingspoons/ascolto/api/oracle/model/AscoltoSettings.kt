package com.bendingspoons.ascolto.api.oracle.model

import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AscoltoSettings(
    // app specific properties
    @field:Json(name = "development_devices") val developmentDevices: List<String> = listOf()
) : OracleSettings()
