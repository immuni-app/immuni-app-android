package org.immuni.android.analytics.session

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class PicoSession(
    val id: String = UUID.randomUUID().toString(),
    val startDate: Date = Date(),
    val sessionData: Map<String, Any> = mapOf(),
    val lastDate: Date? = null,
    val durationMillis: Long = 0,
    val isCrashed: Boolean = false
)
