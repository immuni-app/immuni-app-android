package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.Condition
import com.bendingspoons.ascolto.models.survey.HealthStatusTriage
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawHealthStatusTriage(
    val status: RawHealthStatus,
    val conditions: List<RawConditionItem>
) {
    fun healthStatusTriage() = HealthStatusTriage(
        status = status.healthStatus(),
        condition = Condition(conditions.map { it.conditionItem() })
    )
}
