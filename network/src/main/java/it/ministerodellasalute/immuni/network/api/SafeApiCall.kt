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

import com.squareup.moshi.JsonDataException
import it.ministerodellasalute.immuni.extensions.utils.defaultMoshi
import it.ministerodellasalute.immuni.extensions.utils.fromJson
import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.ResponseBody
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
        val response = result.getOrNull()!!
        if (response.isSuccessful) {
            val responseBody = response.body()
            NetworkResource.Success<T, E>(response, responseBody)
        } else {
            val errorBody = response.errorBody()
            when (errorBody != null) {
                true -> NetworkResource.Error<T, E>(response, NetworkError.HttpError(response.code(), deserializeError(errorBody)))
                false -> NetworkResource.Error<T, E>(response, NetworkError.HttpError(response.code(), null))
            }
        }
    } else {
        val exception = result.exceptionOrNull()
        exception?.printStackTrace()
        return when (exception) {
            is SocketTimeoutException -> NetworkResource.Error(null, NetworkError.Timeout())
            is IOException -> NetworkResource.Error(null, NetworkError.IOError())
            is JsonDataException -> {
                NetworkResource.Error(null, NetworkError.JsonParsingError(exception))
            }
            else -> NetworkResource.Error(null, NetworkError.Unknown())
        }
    }
}

/**
 * Deserialize an error response body using the specified [E] type.
 */
inline fun <reified E : Any> deserializeError(responseBody: ResponseBody): E? {
    val str = responseBody.string()
    val result = runCatching {
        defaultMoshi.fromJson<E>(str)
    }
    return if (result.isSuccess) result.getOrNull()!!
    else null
}
