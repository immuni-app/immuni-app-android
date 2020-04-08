package com.bendingspoons.pico.session

sealed class PicoSessionEventData {
    companion object {
        const val key = "subtype"
    }

    abstract val data: Map<String, Any>
}

class PicoSessionStartData(val id: String) : PicoSessionEventData() {
    companion object {
        const val sessionStartKey = "session_start"
    }

    override val data: Map<String, Any> = mapOf(
        key to sessionStartKey
    )
}

class PicoSessionEndData(
    val sessionStartId: String,
    val crashed: Boolean,
    val duration: Double,
    val sessionData: Map<String, Any>
) : PicoSessionEventData() {
    companion object {
        const val sessionStartIdKey: String = "session_start_id"
        const val sessionEndKey = "session_end"
        const val crashedKey: String = "crashed"
        const val durationKey: String = "duration"
    }

    override val data: Map<String, Any> = sessionData.toMutableMap().apply {
        putAll(
            mapOf(
                key to sessionEndKey,
                sessionStartIdKey to sessionStartId,
                crashedKey to crashed,
                durationKey to duration
            )
        )
    }
}
