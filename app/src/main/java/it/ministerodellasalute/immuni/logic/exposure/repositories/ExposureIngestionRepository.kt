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

package it.ministerodellasalute.immuni.logic.exposure.repositories

import androidx.annotation.VisibleForTesting
import it.ministerodellasalute.immuni.api.immuniApiCall
import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.extensions.utils.sha256
import it.ministerodellasalute.immuni.logic.exposure.models.*
import it.ministerodellasalute.immuni.logic.user.models.Province
import it.ministerodellasalute.immuni.network.api.NetworkError
import it.ministerodellasalute.immuni.network.api.NetworkResource
import java.util.*

class ExposureIngestionRepository(
    private val exposureIngestionService: ExposureIngestionService
) {
    companion object {
        @VisibleForTesting
        fun authorization(otp: String): String = "Bearer ${otp.sha256()}"
        fun authorizationCun(cun: String): String = "Bearer ${("CUN-$cun").sha256()}"
    }

    suspend fun validateOtp(otp: String): OtpValidationResult {
        val response = immuniApiCall {
            exposureIngestionService.validateOtp(
                isDummyData = 0,
                authorization = authorization(otp)
            )
        }
        return when (response) {
            is NetworkResource.Success -> OtpValidationResult.Success(
                OtpToken(otp, response.serverDate!!)
            )
            is NetworkResource.Error -> {
                val errorResponse = response.error
                if (errorResponse is NetworkError.HttpError) {
                    if (errorResponse.httpCode == 401) {
                        OtpValidationResult.Unauthorized
                    } else {
                        OtpValidationResult.ServerError
                    }
                } else {
                    OtpValidationResult.ConnectionError
                }
            }
        }
    }

    suspend fun validateCun(
        cun: String,
        healthInsuranceCard: String,
        symptom_onset_date: String?
    ): CunValidationResult {
        val response = immuniApiCall {
            exposureIngestionService.validateCun(
                isDummyData = 0,
                authorization = authorizationCun(cun),
                body = ExposureIngestionService.ValidateCunRequest(
                    healthInsuranceCard = healthInsuranceCard,
                    symptomOnsetDate = symptom_onset_date
                )
            )
        }
        return when (response) {
            is NetworkResource.Success -> CunValidationResult.Success(
                CunToken(cun, response.serverDate!!)
            )
            is NetworkResource.Error -> {
                val errorResponse = response.error
                if (errorResponse is NetworkError.HttpError) {
                    when (errorResponse.httpCode) {
                        401 -> {
                            CunValidationResult.Unauthorized
                        }
                        409 -> {
                            CunValidationResult.CunAlreadyUsed
                        }
                        else -> {
                            CunValidationResult.ServerError
                        }
                    }
                } else {
                    CunValidationResult.ConnectionError
                }
            }
        }
    }

    suspend fun uploadTeks(
        token: OtpToken?,
        cun: CunToken?,
        province: Province,
        tekHistory: List<ExposureIngestionService.TemporaryExposureKey>,
        exposureSummaries: List<ExposureIngestionService.ExposureSummary>,
        countries: List<String>
    ): Boolean {
        return immuniApiCall {
            exposureIngestionService.uploadTeks(
                systemTime = Date().time.div(1000).toInt(),
                authorization = if (token != null) {
                    authorization(token.otp)
                } else {
                    authorizationCun(cun!!.cun)
                },
                isDummyData = 0,
                body = ExposureIngestionService.UploadTeksRequest(
                    teks = tekHistory,
                    province = province,
                    exposureSummaries = exposureSummaries,
                    countries = countries
                )
            )
        } is NetworkResource.Success
    }

    suspend fun dummyUpload(): Boolean {
        return immuniApiCall {
            exposureIngestionService.validateOtp(
                isDummyData = 1,
                authorization = authorization("DUMMY")
            )
        } is NetworkResource.Success
    }
}
