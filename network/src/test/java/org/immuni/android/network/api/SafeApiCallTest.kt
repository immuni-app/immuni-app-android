package org.immuni.android.network.api

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException

class SafeApiCallTest {

    class MockError {}

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

        coEvery{ mockService.settings() } returns Response.success(responseBody)

        val result = safeApiCall<ResponseBody, MockError> { mockService.settings() }

        assertTrue(result is NetworkResource.Success)
        assertEquals(result.data, responseBody)
    }

    @Test
    fun `test when retrofit call http fails returns proper error`() = runBlockingTest {
        val responseBody = "{}".toResponseBody()

        coEvery{ mockService.settings() } returns Response.error(404, responseBody)

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