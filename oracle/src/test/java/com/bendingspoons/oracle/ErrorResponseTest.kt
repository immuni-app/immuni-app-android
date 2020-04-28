package com.bendingspoons.oracle

import com.bendingspoons.oracle.api.ErrorResponse
import com.bendingspoons.oracle.api.toErrorResponse
import com.bendingspoons.oracle.api.toJson
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import retrofit2.Response

class ErrorResponseTest {
    @Test
    fun errorResponseUnmappableJSON() {
        val response = Response.error<String>(500, "{\"test\":\"test\"}".toResponseBody())
        val error = response.toErrorResponse()
        assertNotNull(error)
        assertEquals(500, error?.httpCode)
    }

    @Test
    fun errorResponseEmptyJSON() {
        val response = Response.error<String>(500, "".toResponseBody())
        val error = response.toErrorResponse()
        assertNotNull(error)
        assertEquals(500, error?.httpCode)
    }

    @Test
    fun errorResponseInvalidJSON() {
        val response = Response.error<String>(500, "{{".toResponseBody())
        val error = response.toErrorResponse()
        assertNotNull(error)
        assertEquals(500, error?.httpCode)
    }

    @Test
    fun testJsonToErrorResponse() {
        val response = Response.error<String>(404, "{\"error\": true, \"error_code\": 504, \"message\": \"myMessage\"}".toResponseBody())
        val error = response.toErrorResponse()
        assertNotNull(error)
        assertEquals(404, error?.httpCode)
        assertEquals(504, error?.errorCode)
        assertEquals("myMessage", error?.message)
    }

    @Test
    fun testErrorResponsetoJson() {
        val error = ErrorResponse(true, errorCode = 504, message = "myMessage")
        val json = error.toJson()
        assertEquals("{\"error\":true,\"message\":\"myMessage\",\"error_code\":504}", json)
    }
}
