package org.immuni.android.analytics.model

import org.immuni.android.analytics.install.PicoInstallEventData
import org.immuni.android.analytics.session.PicoSessionEventData
import java.util.*

sealed class TrackEvent(
    val type: Type,
    val id: String = UUID.randomUUID().toString(),
    val data: Any?
) {
    class Type private constructor(val name: String) {
        companion object {
            val UserAction = Type("UserAction")
            val Session = Type("Session")
            val Install = Type("Install")
            val ExperimentSegmentsReceived = Type("ExperimentSegmentReceived")
            fun Custom(name: String) = Type(name)
        }
    }
}

data class UserAction(
    val actionKind: String,
    val info: Map<String, Any?> = mapOf()
) : TrackEvent(
    Type.UserAction,
    data = mapOf(
        "action_kind" to actionKind,
        "action_info" to info
    )
)

class Session(id: String = UUID.randomUUID().toString(), sessionData: PicoSessionEventData) :
    TrackEvent(Type.Session, id, sessionData.data)

class Install(installData: PicoInstallEventData) : TrackEvent(Type.Install, data = installData)

class ExperimentSegmentsReceived : TrackEvent(Type.ExperimentSegmentsReceived, data = null)

class CustomEvent(type: String, info: Map<String, Any>?) :
    TrackEvent(Type.Custom(type), data = info)
