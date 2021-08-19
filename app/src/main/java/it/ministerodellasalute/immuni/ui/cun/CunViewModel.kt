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

package it.ministerodellasalute.immuni.ui.cun

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.CunToken
import it.ministerodellasalute.immuni.logic.exposure.models.CunValidationResult
import it.ministerodellasalute.immuni.logic.upload.CunValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class CunViewModel(
    val context: Context,
    private val exposureManager: ExposureManager,
    private val cunValidator: CunValidator
) : ViewModel(),
    KoinComponent {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _alertError = MutableLiveData<Event<List<String>>>()
    val alertError: LiveData<Event<List<String>>> = _alertError

    private val _navigateToUploadPage = MutableLiveData<Event<CunToken>>()
    val navigateToUploadPage: LiveData<Event<CunToken>> = _navigateToUploadPage

    fun verifyIndependently(
        cun: String,
        health_insurance_card: String,
        symptom_onset_date: String?
    ) {
        if (checkFormHasError(cun, health_insurance_card, symptom_onset_date)) {
            return
        }
        viewModelScope.launch {
            _loading.value = true

            delay(1000)

            when (val result = exposureManager.validateCun(
                cun, health_insurance_card,
                symptom_onset_date
            )) {
                is CunValidationResult.Success -> {
                    _navigateToUploadPage.value = Event(result.token)
                }
                is CunValidationResult.ServerError -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.upload_data_api_error_title),
                                ""
                            )
                        )
                }
                is CunValidationResult.ConnectionError -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.upload_data_api_error_title),
                                context.getString(R.string.app_setup_view_network_error)
                            )
                        )
                }
                is CunValidationResult.Unauthorized -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.upload_data_api_error_title),
                                context.getString(R.string.cun_unauthorized)
                            )
                        )
                }
                is CunValidationResult.CunAlreadyUsed -> {
                    _alertError.value =
                        Event(
                            listOf(
                                context.getString(R.string.warning_title_modal),
                                context.getString(R.string.cun_already_used)
                            )
                        )
                }
            }

            _loading.value = false
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
