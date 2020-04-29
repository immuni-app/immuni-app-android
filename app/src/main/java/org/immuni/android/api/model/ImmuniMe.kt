package org.immuni.android.api.model

import org.immuni.android.extensions.utils.JSonSerializable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.immuni.android.models.survey.TriageProfileId
import org.immuni.android.networking.api.model.NetworkingMe

@JsonClass(generateAdapter = true)
class ImmuniMe(
    @field:Json(name = "server_triage") val serverTriageProfileId: TriageProfileId? = null
): NetworkingMe(), JSonSerializable
