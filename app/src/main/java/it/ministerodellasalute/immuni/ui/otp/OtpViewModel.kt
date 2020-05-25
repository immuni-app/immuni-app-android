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

package it.ministerodellasalute.immuni.ui.otp

import android.content.Context
import androidx.lifecycle.*
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.OtpToken
import it.ministerodellasalute.immuni.logic.exposure.models.OtpValidationResult
import it.ministerodellasalute.immuni.logic.upload.OtpGenerator
import it.ministerodellasalute.immuni.logic.upload.UploadDisabler
import kotlin.math.round
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class OtpViewModel(
    val context: Context,
    private val uploadDisableManager: UploadDisabler,
    private val otpGenerator: OtpGenerator,
    private val exposureManager: ExposureManager
) : ViewModel(),
    KoinComponent {

    private val _otpCode = MutableLiveData<String>()
    val otpCode: LiveData<String> = _otpCode.map { otpGenerator.prettify(it, " ") }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _verificationError = MutableLiveData<Event<String>>()
    val verificationError: LiveData<Event<String>> = _verificationError

    val buttonDisabledMessage: LiveData<String?> = uploadDisableManager.disabledForSecondsFlow
        .map { it?.toFormattedQuantityText(context) }.asLiveData()

    private val _navigateToUploadPage = MutableLiveData<Event<OtpToken>>()
    val navigateToUploadPage: LiveData<Event<OtpToken>> = _navigateToUploadPage

    init {
        generateNewOtpCode()
    }

    fun verify() {
        viewModelScope.launch {
            _loading.value = true
            val otp = _otpCode.value!!

            delay(1000)

            when (val result = exposureManager.validateOtp(otp)) {
                is OtpValidationResult.Success -> {
                    // On user's successful upload, we need to reset button disabling
                    uploadDisableManager.reset()
                    _navigateToUploadPage.value = Event(result.token)
                }
                is OtpValidationResult.Unauthorized -> {
                    uploadDisableManager.submitFailedAttempt()
                    _verificationError.value =
                        Event(context.getString(R.string.upload_data_verify_error))
                }
                is OtpValidationResult.ServerError -> {
                    _verificationError.value =
                        Event(context.getString(R.string.upload_data_api_error_title))
                }
                is OtpValidationResult.ConnectionError -> {
                    _verificationError.value =
                        Event(context.getString(R.string.app_setup_view_network_error))
                }
            }

            _loading.value = false
        }
    }

    private fun generateNewOtpCode() {
        _otpCode.value = otpGenerator.nextOtpCode()
    }

    private fun Long.toFormattedQuantityText(context: Context): String? {
        return when {
            this in 0..60 -> context.resources.getQuantityString(
                R.plurals.upload_data_verify_loading_button_seconds,
                this.toInt(), this.toInt()
            )
            this > 60 -> {
                val minutes = round(this.toDouble() / 60).toInt()
                context.resources.getQuantityString(
                    R.plurals.upload_data_verify_loading_button_minutes,
                    minutes, minutes
                )
            }
            else -> null
        }
    }
}
