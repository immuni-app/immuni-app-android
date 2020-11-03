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
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ExposureAnalyticsService {
    @JsonClass(generateAdapter = true)
    data class OperationalInfoRequest(
        @field:Json(name = "province") val province: String,
        @field:Json(name = "build") val build: Int,
        @field:Json(name = "exposure_permission") val exposurePermission: Int,
        @field:Json(name = "bluetooth_active") val bluetoothActive: Int,
        @field:Json(name = "notification_permission") val notificationPermission: Int,
        @field:Json(name = "exposure_notification") val exposureNotification: Int,
        @field:Json(name = "last_risky_exposure_on") val lastRiskyExposureOn: String,
        @field:Json(name = "salt") val salt: String,
        @field:Json(name = "signed_attestation") val signedAttestation: String
    )

    @POST("/v1/analytics/google/operational-info")
    suspend fun operationalInfo(
        @Header("Immuni-Dummy-Data") isDummyData: Int,
        @Body body: OperationalInfoRequest
    ): Response<ResponseBody>
}
