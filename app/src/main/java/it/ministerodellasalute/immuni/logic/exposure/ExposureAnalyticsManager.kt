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

package it.ministerodellasalute.immuni.logic.exposure

import android.util.Base64
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.logic.exposure.models.AnalyticsTokenStatus
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsNetworkRepository
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureAnalyticsStoreRepository
import java.security.SecureRandom
import java.util.*

class ExposureAnalyticsManager(
    private val storeRepository: ExposureAnalyticsStoreRepository,
    private val networkRepository: ExposureAnalyticsNetworkRepository,
    private val attestationClient: AttestationClient
) {
    /**
     * Generates random and registers token if needed
     */
    suspend fun setup(serverDate: Date) {
        val token = storeRepository.token
        if (token is AnalyticsTokenStatus.None) {
            storeRepository.token = generateAndValidateToken(serverDate)
        }
    }

    suspend fun onRequestDiagnosisKeysSucceeded() {

    }

    suspend fun generateAndValidateToken(serverDate: Date): AnalyticsTokenStatus {
        val token = ByteArray(32)
        SecureRandom().nextBytes(token)
        val base64Token = Base64.encodeToString(token, Base64.DEFAULT)
        val attestationResponse = attestationClient.attest(base64Token)
        return when (attestationResponse) {
            is AttestationClient.Result.Failure -> AnalyticsTokenStatus.None()
            is AttestationClient.Result.Invalid -> AnalyticsTokenStatus.Invalid()
            is AttestationClient.Result.Success -> {
                val networkResult = networkRepository.validateToken(
                    token = base64Token,
                    attestationPayload = attestationResponse.result
                )
                when (networkResult) {
                    is ExposureAnalyticsNetworkRepository.ValidateTokenResult.Success -> AnalyticsTokenStatus.Valid(
                        base64Token,
                        computeTokenExpiration(serverDate)
                    )
                    is ExposureAnalyticsNetworkRepository.ValidateTokenResult.ValidationError -> AnalyticsTokenStatus.Invalid()
                    is ExposureAnalyticsNetworkRepository.ValidateTokenResult.NetworkError -> AnalyticsTokenStatus.None()
                }


            }
        }
    }

    private fun computeTokenExpiration(serverDate: Date): Date {
        val lastDayOfNextMonth = Calendar.getInstance().apply {
            time = serverDate
            add(Calendar.MONTH, 2)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
        }.get(Calendar.DAY_OF_MONTH)

        val randomDayOfNextMonth = SecureRandom().nextInt(lastDayOfNextMonth) + 1

        return Calendar.getInstance().apply {
            time = serverDate
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, randomDayOfNextMonth)
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}
