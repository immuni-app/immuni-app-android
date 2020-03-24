package org.ascolto.onlus.geocrowd19.android.models

import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.models.survey.TriageProfile
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class User(
    @field:Json(name = "identifier") val id: String,
    @field:Json(name = "age_group") val ageGroup: String = "",
    @field:Json(name = "age") val age: Int = 999,
    @field:Json(name = "gender") val gender: Gender = Gender.FEMALE,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "last_survey_at") val lastSurveyDate: Double? = null,
    @field:Json(name = "last_survey_version") val lastSurveyVersion: String?  = null,
    @field:Json(name = "last_triage_status") val lastTriageProfile: TriageProfile? = null,
    @field:Json(name = "next_survey_at") val nextSurveyDate: Double? = null,
    @field:Json(name = "same_house") val isInSameHouse: Boolean? = null,
    // is_main doesn't arrive from the backend, but we set it later
    @field:Json(name = "is_main") var isMain: Boolean = false
)
