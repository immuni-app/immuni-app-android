package org.immuni.android.models

import android.content.Context
import org.immuni.android.models.survey.TriageProfile
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.models.NicknameType.*
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class User(
    @field:Json(name = "identifier") val id: String = "",
    @field:Json(name = "age_group") val ageGroup: AgeGroup = AgeGroup.ZERO_SEVENTEEN,
    @field:Json(name = "age") val age: Int = 999,
    @field:Json(name = "gender") val gender: Gender = Gender.FEMALE,
    @field:Json(name = "nickname") val nickname: Nickname? = null,
    @field:Json(name = "last_survey_at") val lastSurveyDate: Double? = null,
    @field:Json(name = "last_survey_version") val lastSurveyVersion: String?  = null,
    @field:Json(name = "last_triage_status") val lastTriageProfile: TriageProfile? = null,
    @field:Json(name = "next_survey_at") val nextSurveyDate: Double? = null,
    @field:Json(name = "same_house") val isInSameHouse: Boolean? = null,
    // is_main doesn't arrive from the backend, but we set it later
    @field:Json(name = "is_main") var isMain: Boolean = false
) {
    val name: String
        get() {
            if (isMain) return "-"
            else return nickname?.humanReadable(ImmuniApplication.appContext, gender) ?: "-"
        }
}

enum class AgeGroup {
    @Json(name = "0-17")
    ZERO_SEVENTEEN,
    @Json(name = "18-35")
    EIGHTEEN_THIRTYFIVE,
    @Json(name = "36-45")
    THRITYSIX_FORTYFIVE,
    @Json(name = "46-55")
    FORTYSIX_FIFTYFIVE,
    @Json(name = "56-65")
    FIFTYSIX_SIXTYFIVE,
    @Json(name = "66-75")
    SIXTYSIX_SEVENTYFIVE,
    @Json(name = "75+")
    MORE_THAN_SEVENTYFIVE;

    fun humanReadable(context: Context): String {
        return when (this) {
            ZERO_SEVENTEEN -> context.getString(R.string.age_range_1)
            EIGHTEEN_THIRTYFIVE -> context.getString(R.string.age_range_2)
            THRITYSIX_FORTYFIVE -> context.getString(R.string.age_range_3)
            FORTYSIX_FIFTYFIVE -> context.getString(R.string.age_range_4)
            FIFTYSIX_SIXTYFIVE -> context.getString(R.string.age_range_5)
            SIXTYSIX_SEVENTYFIVE -> context.getString(R.string.age_range_6)
            MORE_THAN_SEVENTYFIVE -> context.getString(R.string.age_range_7)
        }
    }

    val isAdult
        get() = this != ZERO_SEVENTEEN
}

@JsonClass(generateAdapter = true)
data class Nickname(
    @field:Json(name = "type") val type: NicknameType = PARENT,
    @field:Json(name = "value") val value: String? = null
): Serializable {
    fun humanReadable(context: Context, gender: Gender): String {
        return when (Pair(type, gender)) {
            Pair(PARENT, Gender.FEMALE) -> context.getString(R.string.nickname_parent_female)
            Pair(CHILD_1, Gender.FEMALE) -> context.getString(R.string.nickname_child1_female)
            Pair(CHILD_2, Gender.FEMALE) -> context.getString(R.string.nickname_child2_female)
            Pair(CHILD_3, Gender.FEMALE) -> context.getString(R.string.nickname_child3_female)
            Pair(CHILD_4, Gender.FEMALE) -> context.getString(R.string.nickname_child4_female)
            Pair(YOUNGER_SIBLING, Gender.FEMALE) -> context.getString(R.string.nickname_younger_sibling_female)
            Pair(PARTNER, Gender.FEMALE) -> context.getString(R.string.nickname_partner_female)
            Pair(OLDER_SIBLING, Gender.FEMALE) -> context.getString(R.string.nickname_older_sibling_female)
            Pair(PATERNAL_GRANDPARENT, Gender.FEMALE) -> context.getString(R.string.nickname_paternal_grandparent_female)
            Pair(MATERNAL_GRANDPARENT, Gender.FEMALE) -> context.getString(R.string.nickname_maternal_grandparent_female)
            Pair(PARENT, Gender.MALE) -> context.getString(R.string.nickname_parent_male)
            Pair(CHILD_1, Gender.MALE) -> context.getString(R.string.nickname_child1_male)
            Pair(CHILD_2, Gender.MALE) -> context.getString(R.string.nickname_child2_male)
            Pair(CHILD_3, Gender.MALE) -> context.getString(R.string.nickname_child3_male)
            Pair(CHILD_4, Gender.MALE) -> context.getString(R.string.nickname_child4_male)
            Pair(YOUNGER_SIBLING, Gender.MALE) -> context.getString(R.string.nickname_younger_sibling_male)
            Pair(PARTNER, Gender.MALE) -> context.getString(R.string.nickname_partner_male)
            Pair(OLDER_SIBLING, Gender.MALE) -> context.getString(R.string.nickname_older_sibling_male)
            Pair(PATERNAL_GRANDPARENT, Gender.MALE) -> context.getString(R.string.nickname_paternalGrandparent_male)
            Pair(MATERNAL_GRANDPARENT, Gender.MALE) -> context.getString(R.string.nickname_maternalGrandparent_male)
            else -> if (type == OTHER) value!! else context.getString(R.string.nickname_not_specified)
        }
    }
}

enum class NicknameType {
    @Json(name = "partner")
    PARTNER,
    @Json(name = "child1")
    CHILD_1,
    @Json(name = "child2")
    CHILD_2,
    @Json(name = "child3")
    CHILD_3,
    @Json(name = "child4")
    CHILD_4,
    @Json(name = "younger_sibling")
    YOUNGER_SIBLING,
    @Json(name = "older_sibling")
    OLDER_SIBLING,
    @Json(name = "parent")
    PARENT,
    @Json(name = "paternal_grandparent")
    PATERNAL_GRANDPARENT,
    @Json(name = "maternal_grandparent")
    MATERNAL_GRANDPARENT,
    @Json(name = "other")
    OTHER
}
