package com.bendingspoons.oracle.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import retrofit2.Response
import java.lang.Exception

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @field:Json(name = "error") val error: Boolean = false,
    @field:Json(name = "message") val message: String?,
    @field:Json(name = "error_code") val errorCode: Int?
) {
    var httpCode: Int? = null
}

fun Response<*>.toErrorResponse(): ErrorResponse? {
    val error = try {
        val str = this.errorBody()?.string()
        if(str != null) {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter(ErrorResponse::class.java)
            jsonAdapter.fromJson(str)
        } else ErrorResponse(true, "the body is null", null)
    } catch (e: Exception) {
        ErrorResponse(true, e.localizedMessage, null)
    }

    error?.apply {
        httpCode = this@toErrorResponse.code()
    }
    return error
}

fun ErrorResponse.toJson(): String {
    val moshi = Moshi.Builder().build()
    val jsonAdapter = moshi.adapter(ErrorResponse::class.java)
    return jsonAdapter.toJson(this)
}