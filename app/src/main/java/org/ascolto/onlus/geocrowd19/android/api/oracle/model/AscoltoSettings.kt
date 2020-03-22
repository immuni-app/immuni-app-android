package org.ascolto.onlus.geocrowd19.android.api.oracle.model

import org.ascolto.onlus.geocrowd19.android.models.survey.raw.RawSurvey
import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import testSurveyJson

@JsonClass(generateAdapter = true)
class AscoltoSettings(
    // app specific properties
    @field:Json(name = "development_devices") val developmentDevices: List<String> = listOf(),
    @field:Json(name = "reminder_notification_title") val reminderNotificationTitle: String = "",
    @field:Json(name = "reminder_notification_message") val reminderNotificationMessage: String = "",
    @field:Json(name = "survey") val _survey: RawSurvey? = null
) : OracleSettings() {
    val survey: Survey
        //        get() = _survey.survey() // FIXME
        get() = testSurvey().survey()
}

private fun testSurvey(): RawSurvey {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(RawSurvey::class.java)
    val survey = adapter.fromJson(testSurveyJson)
    return survey!!
}
