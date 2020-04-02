package org.immuni.android.api.oracle.model

import com.bendingspoons.base.utils.JSonSerializable
import org.immuni.android.models.User
import com.bendingspoons.oracle.api.model.OracleMe
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class ImmuniMe(
    @field:Json(name = "next_survey_at") val nextSurveyDate: Double? = null,
    @field:Json(name = "householder") val mainUser: User? = null,
    @field:Json(name = "relatives") val familyMembers: List<User> = listOf()
): OracleMe(), JSonSerializable {
    override fun onDeserialize() {
        mainUser?.isMain = true
    }
}
