package com.bendingspoons.pico

import com.bendingspoons.pico.api.model.PicoEventRequest
import com.bendingspoons.pico.model.*
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
                "non_backup_persistent_id" to "",
                "idfa" to ""
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
                installedBeforePico = false,
                isBaseline = false,
                isFree = false,
                timezone = TimezoneInfo(
                    seconds = 0,
                    name = "",
                    daylightSaving = false
                ),
                device = DeviceInfo(
                    androidVersion = "",
                    screenSize = 5.5,
                    platform = ""
                ),
                monetization = MonetizationInfo(
                    isSubscribed = false,
                    availableProductIds = listOf(),
                    customFields = mapOf(
                        "custom_monetization_1" to "CustomMonetizationValue"
                    )
                ),
                experiment = mapOf()
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
    fun `MonetizationInfo's customFields field gets flattened into its parent`() {
        val value = MonetizationInfo(
            isSubscribed = false,
            availableProductIds = listOf(),
            customFields = mapOf(
                "custom_monetization_1" to "CustomMonetizationValue"
            )
        )

        val moshi = Moshi.Builder()
            .add(MonetizationInfoAdapter())
            .build()
        val adapter = moshi.adapter(MonetizationInfo::class.java)
        val json = adapter.toJson(value)

        assertFalse(json.contains("custom_fields"))
        assertTrue(json.contains("custom_monetization_1"))
        assertTrue(json.contains("CustomMonetizationValue"))

        val recreatedValue = adapter.fromJson(json)
        assertEquals(value, recreatedValue)
    }

    @Test
    fun testPicoUserFromEventRequestSerialization() {
        val moshi = Moshi.Builder().add(PicoUserAdapter()).build()
        val jsonAdapter = moshi.adapter(PicoEventRequest::class.java)

        val monetizationInfo = MonetizationInfo(
            isSubscribed = true,
            availableProductIds = listOf(),
            customFields = mapOf(
                "CUSTOM_MONETIZATION_FIELD" to "CUSTOM_MONETIZATION_VALUE"
            )
        )

        val user = PicoUser(
            ids = mutableMapOf(
                "backup_persistent_id" to "123",
                "non_backup_persistent_id" to "456",
                "idfa" to "789"
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
                installedBeforePico = false,
                isFree = true,
                isBaseline = false,
                experiment = mapOf(),
                timezone = TimezoneInfo(
                    seconds = 123,
                    name = "Rome",
                    daylightSaving = true
                ),
                monetization = monetizationInfo,
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

        assertEquals(user.info.monetization, deserializedUser!!.info.monetization)
        assertEquals(user, deserializedUser)

        val deserializedEvent = jsonAdapter.fromJson(json)
        assertEquals(user, deserializedEvent!!.events.first().user)
        assertEquals(event, deserializedEvent)
    }
}
