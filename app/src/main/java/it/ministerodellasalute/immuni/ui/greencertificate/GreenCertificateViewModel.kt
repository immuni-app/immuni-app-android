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

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.DigitValidator
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassToken
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificate
import it.ministerodellasalute.immuni.logic.user.models.User
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class GreenCertificateViewModel(
    val context: Context,
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

    val greenPass = GreenCertificate(
        "06 Gennaio 2021",
        "NCFOXN%TS3DH3ZSUZK+.V0ETD%65NL-AH%TAIOOW%IN5H8B48WAS*PX*GKD93B4:ZH6I1\$4JN:IN1MKK9+OC*PP:+P*.1D9R+Q6646C%6RF6:X93O5RF6\$T61R6B46646VY9WC5ME65H1KD34LT HBSZ4GH0B69X5Q/36MZ5BTMUW5-5QNF6O M9R1RF6ECM676746C0FFS6NWE0Y6Z EJZ6KS6YQEE%61Y6LMEA46B 17PPDFPVX1R270:6NEQ0R6AOM6PPL4Q0RUS-EBHU68E1W5XC52T9UF5LDCPF5RBQ746B46O1N646BQ99Q9E\$B ZJY1B QTOD3CQSP\$SR\$S3NDC9UOD3Y.TJET9G3DZI65BDM14NJ*XI-XIFRLL:F*ZR.OVOIF/HLTNP8EFNC3P:HDD8B1MM1M9NTNC30GH.Z8VHL+KLF%CD 810H% 0R%0ZD5CC9T0H\$/I*44SA3ZX8MWB8XU%7AAVMXQQYUGS6S97T95GW38TW959EGNSMJQ115QTBD MD6U725O2DZ2QQ.961GIMC3DUUKUC5QU8N4D40I5:SIK1O606I:VD30O7KK0"
    )

    fun genera(
        typeToken: String,
        token: String,
        health_insurance: String,
        expiredHealthIDDate: String
    ) {
        if (checkFormHasError(typeToken, token, health_insurance, expiredHealthIDDate)) {
            return
        }
        viewModelScope.launch {
            _loading.value = true
            when (val result = exposureManager.generateGreenCard(
                typeToken, token, health_insurance, expiredHealthIDDate
            )) {
                is GreenPassValidationResult.Success -> {
                    val user = userManager.user
                    userManager.save(
                        User(
                            region = user.value?.region!!,
                            province = user.value?.province!!,
                            greenPass = greenPass
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
        symptom_onset_date: String?
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
        } else if (typeToken.isBlank() && token.isBlank()) {
            message += (context.getString(R.string.form_code_empty) + typeToken)
        } else {
            message += context.getString(R.string.form_type_and_code_empty)
        }

        if (resultValidateToken == GreenPassValidationResult.TokenWrong) {
            message += when (typeToken) {
                "CUN" -> context.getString(R.string.cun_wrong)
                "NRFE" -> context.getString(R.string.nrfe_wrong)
                "NUCG" -> context.getString(R.string.nucg_wrong)
                "OTP" -> context.getString(R.string.otp_wrong)
                else -> context.getString(R.string.otp_wrong)
            }
        }

        if (healthInsuranceCard.isBlank() || healthInsuranceCard.length < 8) {
            message += context.getString(R.string.health_insurance_card_form_error)
        }
        if (symptom_onset_date != null && symptom_onset_date.isBlank()) {
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
}
