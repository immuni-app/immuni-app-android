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

package it.ministerodellasalute.immuni.api.services

import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * Exposure Reporting Service API.
 */
interface ExposureReportingService {
    @JsonClass(generateAdapter = true)
    data class IndexResponse(val oldest: Int, val newest: Int)

    @GET("/v1/keys/index")
    suspend fun index(): Response<IndexResponse>

    @GET("/v1/keys/eu/{Country}/index")
    suspend fun indexEu(@Path("Country") country: String): Response<IndexResponse>

    @Streaming
    @GET("/v1/keys/{chunkNumber}")
    suspend fun chunk(@Path("chunkNumber") chunkNumber: Int): Response<ResponseBody>

    @Streaming
    @GET("/v1/keys/eu/{Country}/{TEKChunkIndex}")
    suspend fun chunkEu(
        @Path("Country") country: String,
        @Path("TEKChunkIndex") chunkNumber: Int
    ): Response<ResponseBody>
}
