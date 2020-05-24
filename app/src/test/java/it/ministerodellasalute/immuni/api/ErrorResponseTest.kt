/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.api

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
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
        val error = ErrorResponse(
            true,
            errorCode = 504,
            message = "myMessage"
        )
        val json = error.toJson()
        assertEquals("{\"error\":true,\"message\":\"myMessage\",\"error_code\":504}", json)
    }
}
