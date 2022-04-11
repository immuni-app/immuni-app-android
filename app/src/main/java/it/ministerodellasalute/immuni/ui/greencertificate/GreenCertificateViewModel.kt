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
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.TestVerificationResult
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult
import it.ministerodellasalute.immuni.logic.greencovidcertificate.DCCManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.util.DigitValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

private const val TAG = "GreenCertificateVM"

@SuppressLint("StaticFieldLeak")
class GreenCertificateViewModel(
    private val context: Context,
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
    private val userManager: UserManager,
    private val digitValidator: DigitValidator,
    private val gcdManager: DCCManager
) : ViewModel(),
    KoinComponent {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _alertError = MutableLiveData<Event<List<String>>>()
    val alertError: LiveData<Event<List<String>>> = _alertError

    private val _navigateToSuccessPage = MutableLiveData<Event<Boolean>>()
    val navigateToSuccessPage: LiveData<Event<Boolean>> = _navigateToSuccessPage

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
            val dateSplitted = expiredHealthIDDate.split("/")
            val dateToPost = dateSplitted.reversed().joinToString("-")
            when (val result = gcdManager.getGreenCard(
                typeToken, token, healthInsurance, dateToPost
            )) {
                is GreenPassValidationResult.Success -> {
                    val user = userManager.user
                    val greenCertificate =
                        decode(decodeImage(result.greenpass.greenPass.toString()))
                    if (greenCertificate == null) {
                        _navigateToSuccessPage.value = Event(false)
                    } else {
                        if (!checkIfExists(greenCertificate, user)) {
                            user.value?.greenPass!!.add(
                                GreenCertificateUser(
                                    base64 = result.greenpass.greenPass.toString(),
                                    data = greenCertificate,
                                    fglTipoDgc = result.greenpass.fglTipoDgc
                                )
                            )
                            userManager.save(
                                User(
                                    region = user.value?.region!!,
                                    province = user.value?.province!!,
                                    greenPass = user.value?.greenPass!!
                                )
                            )
                        }
                        _navigateToSuccessPage.value = Event(true)
                    }
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
                is GreenPassValidationResult.GCDNotFound -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.warning_title_modal),
                                context.getString(R.string.green_certificate_no_dcc_found)
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
                "AUTHCODE" -> digitValidator.validaCheckDigitAuthcode(token)
                "CUEV" -> digitValidator.validaCheckDigitCUEV(token)
                else -> digitValidator.validaCheckDigitAuthcode(token)
            }
        } else if (typeToken.isBlank()) {
            message += context.getString(R.string.form_type_code_empty)
        } else {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.form_code_cun_empty)
                "NRFE" -> context.getString(R.string.form_code_nrfe_empty)
                "NUCG" -> context.getString(R.string.form_code_nucg_empty)
                "AUTHCODE" -> context.getString(R.string.form_code_otp_empty)
                "CUEV" -> context.getString(R.string.form_code_cuev_empty)
                else -> ""
            }
        }

        if (resultValidateToken == GreenPassValidationResult.TokenWrong) {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.form_code_cun_wrong)
                "NRFE" -> context.getString(R.string.form_code_nrfe_wrong)
                "NUCG" -> context.getString(R.string.form_code_nucg_wrong)
                "AUTHCODE" -> context.getString(R.string.form_code_otp_wrong)
                "CUEV" -> context.getString(R.string.form_code_cuev_wrong)
                else -> ""
            }
        } else if (resultValidateToken == GreenPassValidationResult.TokenLengthWrong) {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.form_code_cun_empty)
                "NRFE" -> context.getString(R.string.form_code_nrfe_empty)
                "NUCG" -> context.getString(R.string.form_code_nucg_empty)
                "AUTHCODE" -> context.getString(R.string.form_code_otp_empty)
                "CUEV" -> context.getString(R.string.form_code_cuev_empty)
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

    @SuppressLint("SetTextI18n")
    fun decode(base45: String): GreenCertificate? {
        val greenCertificate: GreenCertificate?
        val verificationResult = VerificationResult()
        val plainInput = prefixValidationService.decode(base45, verificationResult)
        val compressedCose = base45Service.decode(plainInput, verificationResult)
        val cose = compressorService.decode(compressedCose, verificationResult)

        val coseData = coseService.decode(cose, verificationResult)
        if (coseData == null) {
            Log.d(TAG, "Verification failed: COSE not decoded")
            return null
        }

        val kid = coseData.kid
        if (kid == null) {
            Log.d(TAG, "Verification failed: cannot extract kid from COSE")
            return null
        }

        schemaValidator.validate(coseData.cbor, verificationResult)
        greenCertificate = cborService.decode(coseData.cbor, verificationResult)
        validateCertData(greenCertificate, verificationResult)

        return greenCertificate
    }

    private fun decodeImage(encodedImage: String): String {
        val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        val qrcodeImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        var contents = ""
        val intArray = IntArray(qrcodeImage.width * qrcodeImage.height)
        qrcodeImage.getPixels(
            intArray,
            0,
            qrcodeImage.width,
            0,
            0,
            qrcodeImage.width,
            qrcodeImage.height
        )
        val source: LuminanceSource =
            RGBLuminanceSource(qrcodeImage.width, qrcodeImage.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        try {
            val result: Result = reader.decode(bitmap)
            contents = result.text
        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding barcode", e)
        }
        return contents
    }

    private fun validateCertData(
        certificate: GreenCertificate?,
        verificationResult: VerificationResult
    ) {
        certificate?.tests?.let {
            if (it.isNotEmpty()) {
                verificationResult.testVerification =
                    TestVerificationResult(it.first().isTestValid())
            }
        }
    }

    private fun checkIfExists(greenCertificate: GreenCertificate, user: StateFlow<User?>): Boolean {
        var saved = false
        val issuerID = when (true) {
            greenCertificate.tests != null -> {
                greenCertificate.tests!![0].certificateIdentifier
            }
            greenCertificate.vaccinations != null -> {
                greenCertificate.vaccinations!![0].certificateIdentifier
            }
            greenCertificate.recoveryStatements != null -> {
                greenCertificate.recoveryStatements!![0].certificateIdentifier
            }
            greenCertificate.exemptions != null -> {
                greenCertificate.exemptions!![0].certificateIdentifier
            }
            else -> null
        }

        if (issuerID != null) {
            for (greenPass in user.value?.greenPass!!) {
                when (true) {
                    greenPass.data?.tests != null -> {
                        if (greenPass.data?.tests!![0].certificateIdentifier == issuerID) saved =
                            true
                    }
                    greenPass.data?.vaccinations != null -> {
                        if (greenPass.data?.vaccinations!![0].certificateIdentifier == issuerID) saved =
                            true
                    }
                    greenPass.data?.recoveryStatements != null -> {
                        if (greenPass.data?.recoveryStatements!![0].certificateIdentifier == issuerID) saved =
                            true
                    }
                    greenPass.data?.exemptions != null -> {
                        if (greenPass.data?.exemptions!![0].certificateIdentifier == issuerID) saved =
                            true
                    }
                }
            }
        }
        return saved
    }
}
