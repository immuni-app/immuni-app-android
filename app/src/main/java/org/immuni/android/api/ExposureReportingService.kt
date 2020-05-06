package org.immuni.android.api

import okhttp3.ResponseBody
import org.immuni.android.api.model.BtIds
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.api.model.UploadDataRequest
import org.immuni.android.models.ExportData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * App Configuration Service API.
 */
interface ExposureReportingService {

    @GET("/v1/keys/{chunkNumber}")
    suspend fun downloadTemporaryExposureKeys(@Path("chunkNumber") chunkNumber: Int): Response<ImmuniSettings>
}