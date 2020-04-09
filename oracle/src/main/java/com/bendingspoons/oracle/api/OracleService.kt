package com.bendingspoons.oracle.api

import com.bendingspoons.oracle.api.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OracleService {

    @GET("v2/settings")
    suspend fun settings(): Response<ResponseBody>

    @GET("v2/devices/me")
    suspend fun me(): Response<ResponseBody>

    @POST("v2/devices")
    suspend fun devices(@Body request: DevicesRequest): Response<ResponseBody>

    @POST("v2/users/privacy_notice")
    suspend fun privacyNotice(@Body request: PrivacyNoticeRequest): Response<ResponseBody>
}

