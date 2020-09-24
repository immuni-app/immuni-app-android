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

package it.ministerodellasalute.immuni.ui.upload

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.OtpToken
import kotlinx.coroutines.*

class UploadViewModel(
    private val exposureManager: ExposureManager
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _uploadError = MutableLiveData<Event<Boolean>>()
    val uploadError: LiveData<Event<Boolean>> = _uploadError

    private val _uploadSuccess = MutableLiveData<Event<Boolean>>()
    val uploadSuccess: LiveData<Event<Boolean>> = _uploadSuccess

    private val _uploadEuFinish = MutableLiveData<Event<Boolean>>()
    val uploadEuFinish: LiveData<Event<Boolean>> = _uploadEuFinish

    val hasExposureSummaries = exposureManager.hasSummaries

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    fun upload(activity: Activity, token: OtpToken) {
        viewModelScope.launch {
            _loading.value = true
            delay(1000)
            try {
                val isSuccess = exposureManager.uploadTeks(activity, token)
                if (isSuccess) {
                    _uploadSuccess.value = Event(true)
                } else {
                    _uploadError.value = Event(true)
                }
            } catch (e: Exception) {
                _uploadError.value = Event(true)
                e.printStackTrace()
            }
            _loading.value = false
        }
    }

    fun uploadEu(activity: Activity, token: OtpToken) {
        viewModelScope.launch {
            _loading.value = true
            delay(1000)
            try {
                val isSuccess = exposureManager.uploadTeksEu(activity, token)
                if (!isSuccess) {
                    _uploadError.value = Event(true)
                }
                _uploadEuFinish.value = Event(isSuccess)
            } catch (e: Exception) {
                _uploadError.value = Event(true)
                e.printStackTrace()
            }
            _loading.value = false
        }
    }
}
