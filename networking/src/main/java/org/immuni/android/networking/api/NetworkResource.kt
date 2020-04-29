package org.immuni.android.networking.api

import okhttp3.ResponseBody
import org.immuni.android.base.utils.fromJson
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * A generic class that contains data and status about loading this data.
 */
sealed class NetworkResource<T, E>(
    val data: T? = null,
    val error: NetworkError<E>? = null
) {
    class Success<T, E>(data: T) : NetworkResource<T, E>(data = data)
    class Loading<T, E>(data: T? = null) : NetworkResource<T, E>(data = data)
    class Error<T, E>(error: NetworkError<E>) : NetworkResource<T, E>(error = error)
}

sealed class NetworkError<E>(
    val data: E? = null
) {
    class HttpError<E>(val httpCode: Int, data: E?) : NetworkError<E>(data)
    class IOError<E> : NetworkError<E>()
    class Timeout<E> : NetworkError<E>()
    class Unknown<E> : NetworkError<E>()
}

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


inline fun <reified E : Any> deserializeError(responseBody: ResponseBody): E? {
    val str = responseBody.string()
    return fromJson(str)
}