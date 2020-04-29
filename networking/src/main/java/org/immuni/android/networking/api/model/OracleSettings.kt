package org.immuni.android.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class OracleSettings(
    @field:Json(name = "__tos_version__") var tosVersion: String? = null,
    @field:Json(name = "__privacy_notice_version__") var privacyVersion: String? = null,
    @field:Json(name = "min_build_version") var minBuildVersion: Int = 0
)
