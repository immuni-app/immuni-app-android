package org.immuni.android.network.api

/**
 * A generic class that contains data and status about loading this data.
 *
 * @param T success response data type.
 * @param E error response data type.
 */
sealed class NetworkResource<T, E>(
    val data: T? = null,
    val error: NetworkError<E>? = null
) {
    class Success<T, E>(data: T) : NetworkResource<T, E>(data = data)
    class Loading<T, E>(data: T? = null) : NetworkResource<T, E>(data = data)
    class Error<T, E>(error: NetworkError<E>) : NetworkResource<T, E>(error = error)
}

/**
 * A generic network error class.
 *
 * @param E error response data type.
 */
sealed class NetworkError<E>(
    val data: E? = null
) {
    class HttpError<E>(val httpCode: Int, data: E?) : NetworkError<E>(data)
    class IOError<E> : NetworkError<E>()
    class Timeout<E> : NetworkError<E>()
    class Unknown<E> : NetworkError<E>()
}
