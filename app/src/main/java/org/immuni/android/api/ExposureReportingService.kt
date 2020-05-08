package org.immuni.android.api

import org.immuni.android.api.model.ImmuniSettings
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * App Configuration Service API.
 */
interface ExposureReportingService {

    @GET("/v1/keys/{chunkNumber}")
    suspend fun downloadTemporaryExposureKeys(@Path("chunkNumber") chunkNumber: Int): Response<ImmuniSettings>
}
