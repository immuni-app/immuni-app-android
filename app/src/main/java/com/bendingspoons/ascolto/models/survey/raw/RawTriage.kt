package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.*
import com.bendingspoons.ascolto.models.survey.raw.RawSeverity.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawTriage(
    @field:Json(name = "statuses") val statuses: List<RawTriageStatus>,
    @field:Json(name = "logic") val logic: List<RawTriageCondition>
) {
    fun triage() = Triage(
        statuses = statuses.map { it.triageStatus() },
        conditions = logic.map { it.triageCondition() }
    )
}

@JsonClass(generateAdapter = true)
data class RawTriageCondition(
    @field:Json(name = "status") val statusId: TriageStatusId,
    @field:Json(name = "conditions") val conditions: List<RawConditionItem>
) {
    fun triageCondition() = TriageCondition(
        statusId = statusId,
        condition = Condition(conditions.map { it.conditionItem() })
    )
}

@JsonClass(generateAdapter = true)
data class RawTriageStatus(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "severity") val severity: RawSeverity
) {
    fun triageStatus() = TriageStatus(
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
