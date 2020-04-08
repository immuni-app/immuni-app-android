package com.bendingspoons.sesame

import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

private class MockSesameConfiguration(override val secretKey: String) : SesameConfiguration {
}

class SesameTest {

    @Test
    fun sesameSignaturePOST() {

        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBodyString = "{\"events\":[{\"name\":\"Drop Follow Stream\",\"data\":{\"foo\":\"bar\"},\"event\":{\"timestamp\":1446711273.872388,\"id\":1},\"domain\":\"Demo\"}]}"
        val requestBody = requestBodyString.toRequestBody(jsonMediaType)

        val url = "https://api.picolytics.com/v1/record"
        val timestamp = 1446711273888
        val requestID = UUID.fromString("b4752007-4d3f-4d48-a996-96238f1e8b31")
        val expectedHMAC =
            "dc09ab9548fcba2695e859f0f12804d9bdce99baa990f7739308b436622156fd41fb896974d46a3cfcf95bbb2709677415bf12313fd27b4d00609717d08f9be6"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val bodyString = request.bodyString()

        assertEquals(requestBodyString, bodyString)

        val sesame = Sesame(MockSesameConfiguration(secretKey = "1234567890"))
        val signature = sesame.sign(request, timestamp, requestID)

        assertEquals(expectedHMAC, signature)
    }

    @Test
    fun sesameSignatureGET() {

        val url = "https://api.picolytics.com/v1/record"
        val timestamp = 1446711273888
        val requestID = UUID.fromString( "b4752007-4d3f-4d48-a996-96238f1e8b31")
        val expectedHMAC =
            "37a27ef32c4999ffefe1db166eed6a73760f5ee89b7d1854bbe350f0e5af458896b19e3e0e23285a891dffa6b555d49eba1981ec5d3ee8630c0191eb2972e391"

        val request = Request.Builder()
            .url(url)
            .build()

        val sesame = Sesame(MockSesameConfiguration(secretKey = "1234567890"))
        val signature = sesame.sign(request, timestamp, requestID)

        assertEquals(expectedHMAC, signature)
    }
}
