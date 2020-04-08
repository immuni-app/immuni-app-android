package org.immuni.android.api.oracle.model

import com.bendingspoons.base.utils.JSonSerializable
import org.immuni.android.models.User
import com.bendingspoons.oracle.api.model.OracleMe
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class ImmuniMe: OracleMe(), JSonSerializable
