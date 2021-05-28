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
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassToken
import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.util.DigitValidator
import kotlinx.coroutines.delay
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
                    userManager.save(
                        User(
                            region = user.value?.region!!,
                            province = user.value?.province!!,
                            greenPass = result.greenpass.greenPass
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
}
