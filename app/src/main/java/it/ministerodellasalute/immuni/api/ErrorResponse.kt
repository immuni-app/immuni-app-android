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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import it.ministerodellasalute.immuni.extensions.utils.defaultMoshi
import it.ministerodellasalute.immuni.extensions.utils.toJson
import java.lang.Exception
import retrofit2.Response

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @field:Json(name = "error") val error: Boolean = false,
    @field:Json(name = "message") val message: String?,
    @field:Json(name = "error_code") val errorCode: Int?
) {
    var httpCode: Int? = null
}

/**
 * Converts the errorBody of the receiving Retrofit [Response] to [ErrorResponse].
 */
fun Response<*>.toErrorResponse(): ErrorResponse? {
    val error = try {
        val str = this.errorBody()?.string()
        if (str != null) {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(ErrorResponse::class.java)
            jsonAdapter.fromJson(str)
        } else ErrorResponse(
            true,
            "the body is null",
            null
        )
    } catch (e: Exception) {
        ErrorResponse(
            true,
            e.localizedMessage,
            null
        )
    }

    error?.apply {
        httpCode = this@toErrorResponse.code()
    }
    return error
}

/**
 * Converts the receiving [ErrorResponse] to JSON.
 */
fun ErrorResponse.toJson(): String {
    return defaultMoshi.toJson(this)
}
