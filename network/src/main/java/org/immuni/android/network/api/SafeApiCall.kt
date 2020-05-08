package org.immuni.android.network.api

import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.ResponseBody
import org.immuni.android.extensions.utils.fromJson
import retrofit2.Response

/**
 * This method wrap a Retrofit [Response] and handles properly all types of errors.
 * Without this wrapper you should surround with a try/catch every api call to avoid
 * unexpected crashes (no internet connection, timeout).
 *
 * @param block a suspend Retrofit call that returns a [Response]
 * @return a [NetworkResource] response.
 */
suspend inline fun <T, reified E : Any> safeApiCall(block: () -> Response<T>): NetworkResource<T, E> {
    val result = runCatching(block)
    return if (result.isSuccess) {
        val resultValue = result.getOrNull()!!
        if (resultValue.isSuccessful) {
            val responseBody = resultValue.body()
            when (responseBody != null) {
                true -> NetworkResource.Success(responseBody)
                false -> NetworkResource.Error(NetworkError.Unknown())
            }
        } else {
            val errorBody = resultValue.errorBody()
            when (errorBody != null) {
                true -> NetworkResource.Error(NetworkError.HttpError(resultValue.code(), deserializeError(errorBody)))
                false -> NetworkResource.Error(NetworkError.HttpError<E>(resultValue.code(), null))
            }
        }
    } else {
        when (result.exceptionOrNull()) {
            is SocketTimeoutException -> NetworkResource.Error(NetworkError.Timeout())
            is IOException -> NetworkResource.Error(NetworkError.IOError())
            else -> NetworkResource.Error(NetworkError.Unknown())
        }
    }
}

/**
 * Deserialize an error response body using the specified [E] type.
 */
inline fun <reified E : Any> deserializeError(responseBody: ResponseBody): E? {
    val str = responseBody.string()
    val result = runCatching {
        fromJson<E>(str)
    }
    return if (result.isSuccess) result.getOrNull()!!
    else null
}
