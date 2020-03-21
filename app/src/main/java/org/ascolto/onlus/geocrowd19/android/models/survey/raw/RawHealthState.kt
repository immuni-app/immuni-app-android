package org.ascolto.onlus.geocrowd19.android.models.survey.raw

import org.ascolto.onlus.geocrowd19.android.models.survey.*
import org.ascolto.onlus.geocrowd19.android.models.survey.raw.RawHealthStateUpdaterItemType.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class RawHealthStateUpdaterItemType {
    @Json(name = "add")
    ADD,
    @Json(name = "remove")
    REMOVE
}

@JsonClass(generateAdapter = true)
data class RawHealthStateUpdaterItem(
    @field:Json(name = "state") val state: HealthState,
    @field:Json(name = "type") val type: RawHealthStateUpdaterItemType,
    @field:Json(name = "conditions") val conditions: List<RawConditionItem>
) {
    fun healthStateUpdaterItem() = HealthStateUpdaterItem(
        state = state,
        type = when (type) {
            ADD -> HealthStateUpdaterItemType.ADD
            REMOVE -> HealthStateUpdaterItemType.REMOVE
        },
        conditions = conditions.map { it.conditionItem() }
    )
}
