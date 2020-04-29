package org.immuni.android.networking.model

import org.immuni.android.base.utils.JSonSerializable
import org.immuni.android.networking.api.model.OracleMe
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.TriageProfileId

@JsonClass(generateAdapter = true)
class ImmuniMe(
    @field:Json(name = "server_triage") val serverTriageProfileId: TriageProfileId? = null
): OracleMe(), JSonSerializable
