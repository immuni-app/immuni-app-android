package org.ascolto.onlus.geocrowd19.android.api.oracle.model

import org.ascolto.onlus.geocrowd19.android.models.survey.raw.RawSurvey
import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import testSurveyJson

@JsonClass(generateAdapter = true)
class AscoltoSettings(
    // app specific properties
    @field:Json(name = "development_devices") val developmentDevices: List<String> = listOf()
) : OracleSettings()

fun getSettingsSurvey(): RawSurvey? {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(RawSurvey::class.java)
    val survey = adapter.fromJson(testSurveyJson)
    return survey
}
