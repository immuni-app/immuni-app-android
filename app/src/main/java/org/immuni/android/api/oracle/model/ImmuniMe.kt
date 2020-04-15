package org.immuni.android.api.oracle.model

import com.bendingspoons.base.utils.JSonSerializable
import com.bendingspoons.oracle.api.model.OracleMe
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.TriageProfileId
import org.immuni.android.models.survey.raw.RawTriageProfile

@JsonClass(generateAdapter = true)
class ImmuniMe(
    @field:Json(name = "server_triage") val serverTriageProfileId: TriageProfileId?
): OracleMe(), JSonSerializable
