package com.bendingspoons.oracle.api

import com.bendingspoons.oracle.api.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OracleService {

    @GET("settings")
    suspend fun settings(): Response<ResponseBody>

    @GET("devices/me")
    suspend fun me(): Response<ResponseBody>

    @POST("devices")
    suspend fun devices(@Body request: DevicesRequest): Response<ResponseBody>

    @POST("settings/force_experiment")
    suspend fun forceExperiment(@Body request: ForceExperimentRequest): Response<ResponseBody>

    @POST("/users/privacy_notice")
    suspend fun privacyNotice(@Body request: PrivacyNoticeRequest): Response<ResponseBody>

    @POST("users/gift_code/redeem")
    suspend fun redeemGiftCode(@Body request: RedeemGiftCodeRequest): Response<ResponseBody>

    @POST("/transactions/google/bulk")
    suspend fun verifyPurchase(@Body request: VerifyPurchaseRequest): Response<VerifyPurchaseResponse>
}

