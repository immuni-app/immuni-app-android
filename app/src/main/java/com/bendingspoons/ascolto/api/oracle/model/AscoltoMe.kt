package com.bendingspoons.ascolto.api.oracle.model

import com.bendingspoons.ascolto.models.User
import com.bendingspoons.oracle.api.model.OracleMe
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class AscoltoMe(
    @field:Json(name = "next_survey_at") val nextSurveyDate: Date? = null,
    @field:Json(name = "relatives") val familyMembers: List<User> = listOf()
): OracleMe()
