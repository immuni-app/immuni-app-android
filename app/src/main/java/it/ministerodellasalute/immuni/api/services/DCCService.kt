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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DCCService {
    interface RequestWithPadding {
        val padding: String
    }

    @JsonClass(generateAdapter = true)
    data class GreenCardRequest(
        @field:Json(name = "padding") override val padding: String = "",
        @field:Json(name = "token_type") val token_type: String,
        @field:Json(name = "last_his_number") val healthInsuranceCard: String,
        @field:Json(name = "his_expiring_date") val his_expiring_date: String
    ) : RequestWithPadding

    @JsonClass(generateAdapter = true)
    data class GreenCardResponse(val qrcode: String, val fglTipoDgc: String?)

    @POST("v1/ingestion/get-dgc")
    suspend fun getGreenCard(
        @Header("Authorization") authorization: String,
        @Header("Immuni-Dummy-Data") isDummyData: Int,
        @Body body: GreenCardRequest
    ): Response<GreenCardResponse>
}
