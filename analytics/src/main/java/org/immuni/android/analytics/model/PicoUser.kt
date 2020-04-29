package org.immuni.android.analytics.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicoUser(
    @field:Json(name = "ids") val ids: MutableMap<String, String>,
    @field:Json(name = "info") val info: PicoBaseUserInfo,
    @field:Json(name = "additional_info") val additionalInfo: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class PicoBaseUserInfo(
    @field:Json(name = "country") val country: String,
    @field:Json(name = "language") val language: String,
    @field:Json(name = "app_language") val appLanguage: String,
    @field:Json(name = "locale") val locale: String,
    @field:Json(name = "app_version") val appVersion: String,
    @field:Json(name = "bundle_version") val bundleVersion: String,
    @field:Json(name = "first_install_time") val firstInstallTime: Double,
    @field:Json(name = "last_install_time") val lastInstallTime: Double,
    @field:Json(name = "timezone") val timezone: TimezoneInfo,
    @field:Json(name = "device") val device: DeviceInfo
)

@JsonClass(generateAdapter = true)
data class TimezoneInfo(
    @field:Json(name = "seconds") val seconds: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "daylight_saving") val daylightSaving: Boolean
)

@JsonClass(generateAdapter = true)
data class DeviceInfo(
    @field:Json(name = "android_version") val androidVersion: String,
    @field:Json(name = "screen_size") val screenSize: Double,
    @field:Json(name = "platform") val platform: String
)
