package com.bendingspoons.oracle.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForceExperimentRequest(
    @field:Json(name = "experiment_name") val experimentName: String,
    @field:Json(name = "experiment_segment") val experimentSegment: Int
)