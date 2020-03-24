package org.ascolto.onlus.geocrowd19.android.models

import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.models.survey.TriageProfile
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class User(
    @field:Json(name = "identifier") val id: String,
    @field:Json(name = "age_group") val ageGroup: AgeGroup = AgeGroup.ZERO_SEVENTEEN,
    @field:Json(name = "age") val age: Int = 999,
    @field:Json(name = "gender") val gender: Gender = Gender.FEMALE,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "nickname") val nickname: Nickname? = null,
    @field:Json(name = "last_survey_at") val lastSurveyDate: Double? = null,
    @field:Json(name = "last_survey_version") val lastSurveyVersion: String?  = null,
    @field:Json(name = "last_triage_status") val lastTriageProfile: TriageProfile? = null,
    @field:Json(name = "next_survey_at") val nextSurveyDate: Double? = null,
    @field:Json(name = "same_house") val isInSameHouse: Boolean? = null,
    // is_main doesn't arrive from the backend, but we set it later
    @field:Json(name = "is_main") var isMain: Boolean = false
)

enum class AgeGroup(val id: String) {
    @Json(name = "0-17")
    ZERO_SEVENTEEN("0-17"),
    @Json(name = "18-35")
    EIGHTEEN_THIRTYFIVE("18-35"),
    @Json(name = "36-45")
    THRITYSIX_FORTYFIVE("36-45"),
    @Json(name = "46-55")
    FORTYSIX_FIFTYFIVE("46-55"),
    @Json(name = "56-65")
    FIFTYSIX_SIXTYFIVE("56-65"),
    @Json(name = "66-75")
    SIXTYSIX_SEVENTYFIVE("66-75"),
    @Json(name = "75+")
    MORE_THAN_SEVENTYFIVE("75+");

    companion object {
        fun fromId(id: String): AgeGroup = values().first { it.id == id }
    }
}

@JsonClass(generateAdapter = true)
data class Nickname(
    @field:Json(name = "type") val type: NicknameType,
    @field:Json(name = "value") val value: String? = null
)

enum class NicknameType(val id: String) {
    @Json(name = "child1")
    CHILD_1("child1"),
    @Json(name = "child2")
    CHILD_2("child2"),
    @Json(name = "child3")
    CHILD_3("child3"),
    @Json(name = "child4")
    CHILD_4("child4"),
    @Json(name = "parent")
    PARENT("parent"),
    @Json(name = "paternal_grandparent")
    PATERNAL_GRANDPARENT("paternal_grandparent"),
    @Json(name = "maternal_grandparent")
    MATERNAL_GRANDPARENT("maternal_grandparent"),
    @Json(name = "other")
    OTHER("other");

    companion object {
        fun fromId(id: String): NicknameType = values().first { it.id == id }
    }
}
