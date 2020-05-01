package org.immuni.android.networking.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NetworkingService {

    @GET("v1/settings/app")
    suspend fun settings(): Response<ResponseBody>
}

