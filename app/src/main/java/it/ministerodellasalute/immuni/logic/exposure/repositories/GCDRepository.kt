package it.ministerodellasalute.immuni.logic.exposure.repositories

import androidx.annotation.VisibleForTesting
import it.ministerodellasalute.immuni.api.immuniApiCall
import it.ministerodellasalute.immuni.api.services.DCCService
import it.ministerodellasalute.immuni.extensions.utils.sha256
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassToken
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult
import it.ministerodellasalute.immuni.network.api.NetworkError
import it.ministerodellasalute.immuni.network.api.NetworkResource
import java.util.*

class GCDRepository(
    private val gdcService: DCCService
) {
    companion object {
        @VisibleForTesting
        fun authorization(otp: String): String = "Bearer ${otp.sha256()}"
        fun authorizationCun(cun: String): String = "Bearer ${("CUN-$cun").sha256()}"
        fun authorizationNrfe(nrfe: String): String = "Bearer ${nrfe.sha256()}"
        fun authorizationNucg(nucg: String): String = "Bearer ${("NUCG-$nucg").sha256()}"
        fun authorizationCUEV(cuev: String): String = "Bearer ${("CUEV-$cuev").sha256()}"
    }

    suspend fun getGreenCard(
        typeToken: String,
        token: String,
        healthInsurance: String,
        expiredHealthIDDate: String
    ): GreenPassValidationResult {
        val authorization = when (typeToken) {
            "CUN" -> authorizationCun(token)
            "NRFE" -> authorizationNrfe(token)
            "NUCG" -> authorizationNucg(token)
            "OTP" -> authorization(token)
            "CUEV" -> authorizationCUEV(token)
            else -> authorization(token)
        }
        val response = immuniApiCall {
            gdcService.getGreenCard(
                isDummyData = 0,
                authorization = authorization,
                body = DCCService.GreenCardRequest(
                    token_type = typeToken.toLowerCase(Locale.ROOT),
                    healthInsuranceCard = healthInsurance,
                    his_expiring_date = expiredHealthIDDate
                )

            )
        }
        return when (response) {
            is NetworkResource.Success -> GreenPassValidationResult.Success(
                GreenPassToken(response.data?.qrcode, response.data?.fglTipoDgc, response.serverDate!!)
            )
            is NetworkResource.Error -> {
                val errorResponse = response.error
                if (errorResponse is NetworkError.HttpError) {
                    when (errorResponse.httpCode) {
                        404 -> {
                            GreenPassValidationResult.GCDNotFound
                        }
                        else -> {
                            GreenPassValidationResult.ServerError
                        }
                    }
                } else {
                    GreenPassValidationResult.ConnectionError
                }
            }
        }
    }
}
