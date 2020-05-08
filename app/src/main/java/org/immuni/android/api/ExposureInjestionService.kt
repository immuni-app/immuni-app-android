package org.immuni.android.api

import okhttp3.ResponseBody
import org.immuni.android.api.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Exposoure Injestion Service API.
 */
interface ExposureInjestionService {
    @POST("v1/upload")
    suspend fun uploadDeviceData(@Body upload: UploadDeviceDataRequest): Response<ResponseBody>

    @POST("/v1/tek")
    suspend fun requireTekUpload(@Body upload: UploadDataRequest): Response<ResponseBody>
}
