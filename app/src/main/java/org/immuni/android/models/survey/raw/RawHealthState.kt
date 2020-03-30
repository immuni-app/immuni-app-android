package org.immuni.android.models.survey.raw

import org.immuni.android.models.survey.*
import org.immuni.android.models.survey.raw.RawHealthStateUpdaterItemType.*
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
    @field:Json(name = "condition") val condition: RawCondition
) {
    fun healthStateUpdaterItem() = HealthStateUpdaterItem(
        state = state,
        type = when (type) {
            ADD -> HealthStateUpdaterItemType.ADD
            REMOVE -> HealthStateUpdaterItemType.REMOVE
        },
        condition = condition.condition()
    )
}
