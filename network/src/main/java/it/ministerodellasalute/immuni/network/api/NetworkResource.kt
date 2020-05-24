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
import it.ministerodellasalute.immuni.extensions.utils.DateUtils
import java.util.*
import retrofit2.Response

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
    class Success<T, E>(
        val response: Response<T>,
        data: T? = null
    ) : NetworkResource<T, E>(data = data) {
        val serverDate: Date?
            get() {
                val serverDateString = response.headers()["Date"] ?: return null
                return DateUtils.parseHttpDate(serverDateString)
            }
    }

    class Error<T, E>(val response: Response<T>?, error: NetworkError<E>) :
        NetworkResource<T, E>(error = error)
}

/**
 * A generic network error class.
 *
 * @param E error response data type.
 */
sealed class NetworkError<E> {
    class HttpError<E>(val httpCode: Int, val data: E?) : NetworkError<E>()
    class IOError<E> : NetworkError<E>()
    class Timeout<E> : NetworkError<E>()
    class Unknown<E> : NetworkError<E>()
    class JsonParsingError<E>(val exception: JsonDataException) : NetworkError<E>()
}
