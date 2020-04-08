package com.bendingspoons.pico

import com.bendingspoons.pico.api.model.PicoEventRequest
import com.bendingspoons.pico.model.*
import com.bendingspoons.pico.util.BS_NUMBER_JSON_SERIALIZATION_ADAPTER
import com.squareup.moshi.*
import org.junit.Assert.assertEquals
import org.junit.Test


class PicoEventRequestTest {

    @Test
    fun `test pico event request serialization`() {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(PicoEventRequest::class.java)

        val event = PicoEventRequest(
            delta = 12,
            lastEventTimestamp = 12121212.12,
            events = listOf(
                PicoEvent(
                    id = "123",
                    timestamp = 3232323.3232,
                    requestTimestamp = 32323232.232,
                    app = "my-app",
                    user = PicoUser(
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
                            monetization = MonetizationInfo(
                                isSubscribed = true,
                                availableProductIds = listOf(),
                                customFields = mapOf()
                            ),
                            device = DeviceInfo(
                                androidVersion = "32",
                                screenSize = 5.5,
                                platform = "android"
                            )
                        ),
                        additionalInfo = mapOf()
                    ),
                    type = TrackEvent.Type.UserAction.name,
                    data = mapOf("key" to mapOf("subkey" to 4))
                )
            )
        )

        val json = jsonAdapter.toJson(event)

        val expected = """{"delta":12,"last_event_timestamp":1.212121212E7,"events":[{"id":"123","timestamp":3232323.3232,"request_timestamp":3.2323232232E7,"app":"my-app","user":{"ids":{"backup_persistent_id":"123","non_backup_persistent_id":"456","idfa":"789"},"info":{"country":"US","language":"en","app_language":"en","locale":"en","app_version":"0.0.1","bundle_version":"1","first_install_time":0.0,"last_install_time":0.0,"installed_before_pico":false,"is_baseline":false,"is_free":true,"timezone":{"seconds":123,"name":"Rome","daylight_saving":true},"device":{"android_version":"32","screen_size":5.5,"platform":"android"},"monetization":{"is_subscribed":true,"available_product_ids":[],"custom_fields":{}},"experiment":{}},"additional_info":{}},"type":"UserAction","data":{"key":{"subkey":4}}}]}"""

        assertEquals(expected, json)
    }

    @Test
    fun `test pico event request deserialization and serialization with custom number formatting`() {
        val moshi = Moshi.Builder().add(BS_NUMBER_JSON_SERIALIZATION_ADAPTER).build()
        val jsonAdapter = moshi.adapter(PicoEventRequest::class.java)

        val json = """{"delta":12,"last_event_timestamp":1.212121212E7,"events":[{"id":"123","timestamp":3232323.3232,"request_timestamp":3.2323232232E7,"app":"my-app","user":{"ids":{"backup_persistent_id":"123","non_backup_persistent_id":"456","idfa":"789"},"info":{"country":"US","language":"en","app_language":"en","locale":"en","app_version":"0.0.1","bundle_version":"1","first_install_time":0.0,"last_install_time":0.0,"installed_before_pico":false,"is_baseline":false,"is_free":true,"timezone":{"seconds":123,"name":"Rome","daylight_saving":true},"device":{"android_version":"32","screen_size":5.5,"platform":"android"},"monetization":{"is_subscribed":true,"available_product_ids":[],"custom_fields":{}},"experiment":{}},"additional_info":{}},"type":"UserAction","data":{"key":{"subkey":4}}}]}"""

        val event = jsonAdapter.fromJson(json)

        assertEquals(json, jsonAdapter.toJson(event))
    }
}
