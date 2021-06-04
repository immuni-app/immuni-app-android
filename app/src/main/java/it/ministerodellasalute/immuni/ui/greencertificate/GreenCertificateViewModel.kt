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

package it.ministerodellasalute.immuni.ui.greencertificate

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.TestVerificationResult
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassToken
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult
import it.ministerodellasalute.immuni.logic.greencovidcertificate.model.*
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.util.DigitValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent

private const val TAG = "GreenCertificateVM"

@SuppressLint("StaticFieldLeak")
class GreenCertificateViewModel(
    private val context: Context,
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val cryptoService: CryptoService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
    private val exposureManager: ExposureManager,
    private val userManager: UserManager,
    private val digitValidator: DigitValidator
) : ViewModel(),
    KoinComponent {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _alertError = MutableLiveData<Event<List<String>>>()
    val alertError: LiveData<Event<List<String>>> = _alertError

    private val _navigateToSuccessPage = MutableLiveData<Event<GreenPassToken>>()
    val navigateToSuccessPage: LiveData<Event<GreenPassToken>> = _navigateToSuccessPage

    fun genera(
        typeToken: String,
        token: String,
        healthInsurance: String,
        expiredHealthIDDate: String
    ) {
        if (checkFormHasError(typeToken, token, healthInsurance, expiredHealthIDDate)) {
            return
        }
        viewModelScope.launch {
            _loading.value = true

            delay(1000)

            when (val result = exposureManager.getGreenCard(
                typeToken, token, healthInsurance, expiredHealthIDDate
            )) {
                is GreenPassValidationResult.Success -> {

                    val user = userManager.user
                    user.value?.greenPass!!.add(decodeGreenPass(result.greenpass.greenPass))
                    userManager.save(
                        User(
                            region = user.value?.region!!,
                            province = user.value?.province!!,
                            greenPass = user.value?.greenPass!!
                        )
                    )
                    _navigateToSuccessPage.value = Event(result.greenpass)
                }
                is GreenPassValidationResult.ServerError -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.upload_data_api_error_title),
                                ""
                            )
                        )
                }
                is GreenPassValidationResult.ConnectionError -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.upload_data_api_error_title),
                                context.getString(R.string.app_setup_view_network_error)
                            )
                        )
                }
                is GreenPassValidationResult.Unauthorized -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.upload_data_api_error_title),
                                context.getString(R.string.cun_unauthorized)
                            )
                        )
                }
            }

            _loading.value = false
        }
    }

    private fun checkFormHasError(
        typeToken: String,
        token: String,
        healthInsuranceCard: String,
        expiredHealthIDDate: String?
    ): Boolean {
        var message = ""
        var resultValidateToken: GreenPassValidationResult? = null
        if (typeToken.isNotBlank() && token.isNotBlank()) {
            resultValidateToken = when (typeToken) {
                "CUN" -> digitValidator.validaCheckDigitCUN(token)
                "NRFE" -> digitValidator.validaCheckDigitNRFE(token)
                "NUCG" -> digitValidator.validaCheckDigitNUCG(token)
                "OTP" -> digitValidator.validaCheckDigitOTP(token)
                else -> digitValidator.validaCheckDigitOTP(token)
            }
        } else if (typeToken.isBlank()) {
            message += context.getString(R.string.form_type_code_empty)
        } else {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.form_code_cun_empty)
                "NRFE" -> context.getString(R.string.form_code_nrfe_empty)
                "NUCG" -> context.getString(R.string.form_code_nucg_empty)
                "OTP" -> context.getString(R.string.form_code_otp_empty)
                else -> ""
            }
        }

        if (resultValidateToken == GreenPassValidationResult.TokenWrong) {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.form_code_cun_wrong)
                "NRFE" -> context.getString(R.string.form_code_nrfe_wrong)
                "NUCG" -> context.getString(R.string.form_code_nucg_wrong)
                "OTP" -> context.getString(R.string.form_code_otp_wrong)
                else -> ""
            }
        } else if (resultValidateToken == GreenPassValidationResult.TokenLengthWrong) {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.form_code_cun_empty)
                "NRFE" -> context.getString(R.string.form_code_nrfe_empty)
                "NUCG" -> context.getString(R.string.form_code_nucg_empty)
                "OTP" -> context.getString(R.string.form_code_otp_empty)
                else -> ""
            }
        }

        if (healthInsuranceCard.isBlank() || healthInsuranceCard.length < 8) {
            message += context.getString(R.string.health_insurance_card_form_error)
        }
        if (expiredHealthIDDate != null && expiredHealthIDDate.isBlank()) {
            message += context.getString(R.string.form_expired_health_date)
        }
        if (message.isNotEmpty()) {
            _alertError.value = Event(
                listOf(
                    context.getString(R.string.dialog_error_form_title),
                    message
                )
            )
            return true
        }
        return false
    }

    private fun decodeGreenPass(greenPass: String): GreenCertificateUser {
        val base45 = "HC1:NCFOXN%TS3DH3ZSUZK+.V0ETD%65NL-AH-R6IOOP-IIDTMB9GJL1%BJ.4+QI6M8SA3/-2E%5VR5VVB9ZILAPIZI.EJJ14B2MZ8DC8COVD9VC/MJK.A+ C/8DXED%JCC8C62KXJAUYCOS2QW6%PQRZMPK9I+0MCIKYJGCC:H3J1D1I3-*TW CXBDW33+ CD8CQ8C0EC%*TGHD1KT0NDPST7KDQHDN8TSVD2NDB*S6ECX%LBZI+PB/VSQOL9DLKWCZ3EBKD8IIGDB0D48UJ06J9UBSVAXCIF4LEIIPBJ7OICWK%5BBS22T9UF5LDCPF5RBQ746B46JZ0V-OEA7IB6" + "$" + "C94JB2E9Z3E8AE-QD+PB.QCD-H/8O3BEQ8L9VN.6A4JBLHLM7A"+"$"+"JD IBCLCK5MJMS68H36DH.K:Z28AL**I3DN3F7MHFEVV%*4HBTSCNT 4C%C47TO*47*KB*KYQT3LT+*4." + "$" + "S6ZC0JB%JB% NHTC:OS:DT887WA6+1V/FOX048H6HT2D:M%M3 -IKYAT03-KFL"+"$"+"T6QM*17H.3RO6-BBC%F+KTW:LKO3*QTHSP1*7APSOP8WJGV"+"$"+"FPUU5U1ZN2X%ERMH"
        decode(base45)
        return GreenCertificateUser(
            base64 = greenPass,
            vaccineName = "Moderna",
            dosesNumber = "1",
            totalDosesNumber = "2"
        )
    }

    @SuppressLint("SetTextI18n")
    fun decode(code: String) {
        viewModelScope.launch {
            _loading.value = true
            var greenCertificate: GreenCertificate? = null
            val verificationResult = VerificationResult()

            withContext(Dispatchers.IO) {
                val plainInput = prefixValidationService.decode(code, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose = compressorService.decode(compressedCose, verificationResult)

                val coseData = coseService.decode(cose, verificationResult)
                if (coseData == null) {
                    Log.d(TAG, "Verification failed: COSE not decoded")
                    return@withContext
                }

                val kid = coseData.kid
                if (kid == null) {
                    Log.d(TAG, "Verification failed: cannot extract kid from COSE")
                    return@withContext
                }

                schemaValidator.validate(coseData.cbor, verificationResult)
                greenCertificate = cborService.decode(coseData.cbor, verificationResult)
                validateCertData(greenCertificate, verificationResult)


//                // Load from API for now. Replace with cache logic.
//                val certificate = exposureManager.getCertificate(kid.toBase64())

//                if (certificate == null) {
//                    Log.d(TAG, "Verification failed: failed to load certificate")
//                    return@withContext
//                }
//                cryptoService.validate(cose, certificate, verificationResult)
            }
            _loading.value = false
            val cert = greenCertificate?.toCertificateModel()
            val verRes = verificationResult

            var d = ""
        }
    }

    private fun validateCertData(certificate: GreenCertificate?, verificationResult: VerificationResult) {
        certificate?.tests?.let {
            if (it.isNotEmpty()) {
                verificationResult.testVerification = TestVerificationResult(it.first().isTestValid())
            }
        }
    }
}
