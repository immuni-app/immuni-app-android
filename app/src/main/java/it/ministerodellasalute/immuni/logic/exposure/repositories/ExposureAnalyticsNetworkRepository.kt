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

import it.ministerodellasalute.immuni.api.immuniApiCall
import it.ministerodellasalute.immuni.api.services.ExposureAnalyticsService
import it.ministerodellasalute.immuni.network.api.NetworkError
import it.ministerodellasalute.immuni.network.api.NetworkResource

class ExposureAnalyticsNetworkRepository(
    private val service: ExposureAnalyticsService
) {
    sealed class ValidateTokenResult {
        object Success : ValidateTokenResult()
        object ValidationError : ValidateTokenResult()
        object NetworkError : ValidateTokenResult()
    }

    suspend fun validateToken(token: String, attestationPayload: String): ValidateTokenResult {
        val response = immuniApiCall { service.token(
            ExposureAnalyticsService.TokenRequest(
                analyticsToken = token,
                deviceToken = attestationPayload
            )
        )}

        return when(response) {
            is NetworkResource.Success -> ValidateTokenResult.Success
            is NetworkResource.Error -> {
                when(val error = response.error) {
                    is NetworkError.HttpError -> {
                        if (error.httpCode == 400) { // FIXME
                            ValidateTokenResult.ValidationError
                        } else {
                            ValidateTokenResult.NetworkError
                        }
                    }
                    else -> ValidateTokenResult.NetworkError
                }
            }
        }
    }
}
