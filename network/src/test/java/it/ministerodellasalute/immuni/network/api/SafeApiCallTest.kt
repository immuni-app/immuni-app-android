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

package it.ministerodellasalute.immuni.network.api

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SafeApiCallTest {

    class MockError

    interface MockService {
        fun settings(): Response<ResponseBody>
    }

    @MockK(relaxed = true)
    lateinit var mockService: MockService

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `test when retrofit call succeed returns success`() = runBlockingTest {

        val responseBody = "{}".toResponseBody()

        coEvery { mockService.settings() } returns Response.success(responseBody)

        val result = safeApiCall<ResponseBody, MockError> { mockService.settings() }

        assertTrue(result is NetworkResource.Success)
        assertEquals(result.data, responseBody)
    }

    @Test
    fun `test when retrofit call http fails returns proper error`() = runBlockingTest {
        val responseBody = "{}".toResponseBody()

        coEvery { mockService.settings() } returns Response.error(404, responseBody)

        val result = safeApiCall<ResponseBody, MockError> { mockService.settings() }

        assertTrue(result is NetworkResource.Error)
        assertTrue(result.error is NetworkError.HttpError)
        assertEquals(404, (result.error as NetworkError.HttpError).httpCode)
    }

    @Test
    fun `test when retrofit call IO fails returns proper error`() = runBlockingTest {

        val result = safeApiCall<ResponseBody, MockError> { throw IOException() }

        assertTrue(result is NetworkResource.Error)
        assertTrue(result.error is NetworkError.IOError)
    }

    @Test
    fun `test when retrofit call timeout fail returns proper error`() = runBlockingTest {
        val result = safeApiCall<ResponseBody, MockError> { throw SocketTimeoutException() }

        assertTrue(result is NetworkResource.Error)
        assertTrue(result.error is NetworkError.Timeout)
    }

    @Test
    fun `test when retrofit call fails for unknown reasons returns proper error`() = runBlockingTest {
        val result = safeApiCall<ResponseBody, MockError> { throw Exception() }

        assertTrue(result is NetworkResource.Error)
        assertTrue(result.error is NetworkError.Unknown)
    }
}
