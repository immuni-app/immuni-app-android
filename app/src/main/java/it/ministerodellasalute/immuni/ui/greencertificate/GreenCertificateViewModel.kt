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
import it.ministerodellasalute.immuni.logic.exposure.models.CunValidationResult
import it.ministerodellasalute.immuni.logic.upload.CunValidator
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificate
import it.ministerodellasalute.immuni.logic.user.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class GreenCertificateViewModel(
    val context: Context,
    private val userManager: UserManager,
    private val cunValidator: CunValidator
) : ViewModel(),
    KoinComponent {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _alertError = MutableLiveData<Event<List<String>>>()
    val alertError: LiveData<Event<List<String>>> = _alertError

    private val _navigateToSuccessPage = MutableLiveData<Event<Boolean>>()
    val navigateToSuccessPage: LiveData<Event<Boolean>> = _navigateToSuccessPage

    val greenPass = GreenCertificate(
        "06 Gennaio 2021",
        "NCFOXN%TS3DH3ZSUZK+.V0ETD%65NL-AH%TAIOOW%IN5H8B48WAS*PX*GKD93B4:ZH6I1\$4JN:IN1MKK9+OC*PP:+P*.1D9R+Q6646C%6RF6:X93O5RF6\$T61R6B46646VY9WC5ME65H1KD34LT HBSZ4GH0B69X5Q/36MZ5BTMUW5-5QNF6O M9R1RF6ECM676746C0FFS6NWE0Y6Z EJZ6KS6YQEE%61Y6LMEA46B 17PPDFPVX1R270:6NEQ0R6AOM6PPL4Q0RUS-EBHU68E1W5XC52T9UF5LDCPF5RBQ746B46O1N646BQ99Q9E\$B ZJY1B QTOD3CQSP\$SR\$S3NDC9UOD3Y.TJET9G3DZI65BDM14NJ*XI-XIFRLL:F*ZR.OVOIF/HLTNP8EFNC3P:HDD8B1MM1M9NTNC30GH.Z8VHL+KLF%CD 810H% 0R%0ZD5CC9T0H\$/I*44SA3ZX8MWB8XU%7AAVMXQQYUGS6S97T95GW38TW959EGNSMJQ115QTBD MD6U725O2DZ2QQ.961GIMC3DUUKUC5QU8N4D40I5:SIK1O606I:VD30O7KK0"
    )

    fun genera(
        cun: String,
        health_insurance_card: String,
        symptom_onset_date: String?
    ) {
//        if (checkFormHasError(cun, health_insurance_card, symptom_onset_date)) {
//            return
//        }
        viewModelScope.launch {
            _loading.value = true

            delay(1000)
            val user = userManager.user
            userManager.save(
                User(
                    region = user.value?.region!!,
                    province = user.value?.province!!,
                    greenPass = greenPass
                )
            )
            _navigateToSuccessPage.value = Event(true)
            _loading.value = false

//            when (val result = exposureManager.validateCun(
//                cun, health_insurance_card,
//                symptom_onset_date
//            )) {
//                is CunValidationResult.Success -> {
//                    _navigateToSuccessPage.value = Event(result.token)
//                }
//                is CunValidationResult.ServerError -> {
//                    _alertError.value =
//                        Event(
//                            listOf(
//                                context.getString(R.string.upload_data_api_error_title),
//                                ""
//                            )
//                        )
//                }
//                is CunValidationResult.ConnectionError -> {
//                    _alertError.value =
//                        Event(
//                            listOf(
//                                context.getString(R.string.upload_data_api_error_title),
//                                context.getString(R.string.app_setup_view_network_error)
//                            )
//                        )
//                }
//                is CunValidationResult.Unauthorized -> {
//                    _alertError.value =
//                        Event(
//                            listOf(
//                                context.getString(R.string.upload_data_api_error_title),
//                                context.getString(R.string.cun_unauthorized)
//                            )
//                        )
//                }
//                is CunValidationResult.CunAlreadyUsed -> {
//                    _alertError.value =
//                        Event(
//                            listOf(
//                                context.getString(R.string.warning_title_modal),
//                                context.getString(R.string.cun_already_used)
//                            )
//                        )
//                }
//            }
//
//            _loading.value = false
        }
    }

    private fun checkFormHasError(
        cun: String,
        healthInsuranceCard: String,
        symptom_onset_date: String?
    ): Boolean {
        var message = ""

        if (cun.isBlank() || cun.length < 10) {
            message += context.getString(R.string.cun_form_error)
        } else if (cunValidator.validaCheckDigitCUN(cun) == CunValidationResult.CunWrong) {
            message += context.getString(R.string.cun_wrong)
        }
        if (healthInsuranceCard.isBlank() || healthInsuranceCard.length < 8) {
            message += context.getString(R.string.health_insurance_card_form_error)
        }
        if (symptom_onset_date != null && symptom_onset_date.isBlank()) {
            message += context.getString(R.string.symptom_onset_date_form_error)
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