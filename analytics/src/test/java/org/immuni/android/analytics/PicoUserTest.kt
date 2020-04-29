package org.immuni.android.analytics

import org.immuni.android.analytics.api.model.PicoEventRequest
import org.immuni.android.analytics.model.*
import com.squareup.moshi.Moshi
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PicoUserTest {
    @Test
    fun `PicoUser's additionalInfo gets merged into info when serialized to json and viceversa`() {
        val value = PicoUser(
            ids = mutableMapOf(
                "backup_persistent_id" to "",
                "non_backup_persistent_id" to ""
            ),
            info = PicoBaseUserInfo(
                country = "",
                language = "",
                appLanguage = "",
                locale = "",
                appVersion = "",
                bundleVersion = "",
                firstInstallTime = 0.0,
                lastInstallTime = 0.0,
                timezone = TimezoneInfo(
                    seconds = 0,
                    name = "",
                    daylightSaving = false
                ),
                device = DeviceInfo(
                    androidVersion = "",
                    screenSize = 5.5,
                    platform = ""
                )
            ),
            additionalInfo = mapOf(
                "field_1" to "Hello",
                "field_2" to 3.0
            )
        )

        val moshi = Moshi.Builder()
            .add(PicoUserAdapter())
            .build()
        val adapter = moshi.adapter(PicoUser::class.java)
        val json = adapter.toJson(value)

        assertFalse(json.contains("additional_info"))
        assertTrue(json.contains("field_1"))
        assertTrue(json.contains("Hello"))
        assertTrue(json.contains("field_2"))

        val recreatedValue = adapter.fromJson(json)
        assertEquals(value, recreatedValue)
    }

    @Test
    fun testPicoUserFromEventRequestSerialization() {
        val moshi = Moshi.Builder().add(PicoUserAdapter()).build()
        val jsonAdapter = moshi.adapter(PicoEventRequest::class.java)

        val user = PicoUser(
            ids = mutableMapOf(
                "backup_persistent_id" to "123",
                "non_backup_persistent_id" to "456"
            ),
            info = PicoBaseUserInfo(
                country = "US",
                language = "en",
                appLanguage = "en",
                appVersion = "0.0.1",
                bundleVersion = "1",
                locale = "en",
                firstInstallTime = 0.0,
                lastInstallTime = 0.0,
                timezone = TimezoneInfo(
                    seconds = 123,
                    name = "Rome",
                    daylightSaving = true
                ),
                device = DeviceInfo(
                    androidVersion = "32",
                    screenSize = 5.5,
                    platform = "android"
                )
            ),
            additionalInfo = mapOf(
                "CUSTOM_FIELD" to "CUSTOM_VALUE"
            )
        )

        val event = PicoEventRequest(
            delta = 12,
            lastEventTimestamp = 12121212.12,
            events = listOf(
                PicoEvent(
                    id = "123",
                    timestamp = 3232323.3232,
                    requestTimestamp = 32323232.232,
                    app = "my-app",
                    user = user,
                    type = TrackEvent.Type.UserAction.name,
                    data = mapOf("key" to mapOf("subkey" to "subvalue"))
                )
            )
        )

        val json = jsonAdapter.toJson(event)
        val jsonMap = jsonAdapter.toJsonValue(event) as Map<String, Any>
        val userMap = (jsonMap["events"] as List<Map<String, Any>>).firstOrNull()?.get("user")
        val deserializedUser = moshi.adapter<PicoUser>(PicoUser::class.java).fromJsonValue(userMap)

        assertEquals(user, deserializedUser)

        val deserializedEvent = jsonAdapter.fromJson(json)
        assertEquals(user, deserializedEvent!!.events.first().user)
        assertEquals(event, deserializedEvent)
    }
}
