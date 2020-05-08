package org.immuni.android.models.survey.raw

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.*
import org.immuni.android.models.survey.raw.RawSeverity.*

@JsonClass(generateAdapter = true)
data class RawTriage(
    @field:Json(name = "profiles") val profiles: List<RawTriageProfile>,
    @field:Json(name = "logic") val logic: List<RawTriageCondition>
) {
    fun triage() = Triage(
        profiles = profiles.map { it.triageStatus() },
        conditions = logic.map { it.triageCondition() }
    )
}

@JsonClass(generateAdapter = true)
data class RawTriageCondition(
    @field:Json(name = "profile_id") val profileId: TriageProfileId,
    @field:Json(name = "condition") val condition: RawCondition
) {
    fun triageCondition() = TriageCondition(
        profileId = profileId,
        condition = condition.condition()
    )
}

@JsonClass(generateAdapter = true)
data class RawTriageProfile(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "severity") val severity: RawSeverity
) {
    fun triageStatus() = TriageProfile(
        id = id,
        url = url,
        severity = when (severity) {
            LOW -> Severity.LOW
            MID -> Severity.MID
            HIGH -> Severity.HIGH
        }
    )
}

enum class RawSeverity {
    @Json(name = "low")
    LOW,
    @Json(name = "mid")
    MID,
    @Json(name = "high")
    HIGH
}
