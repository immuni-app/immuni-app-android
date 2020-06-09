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

package it.ministerodellasalute.immuni.ui.settings

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.models.FetchFaqsResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.KoinComponent

class SettingsViewModel(
    private val settingsManager: ConfigurationSettingsManager
) : ViewModel(), KoinComponent {

    companion object {
        const val EXPOSURE_NOTIFICATION_SETTINGS_REQUEST = 2206
    }

    val navigateToFaqs = MutableLiveData<Event<Boolean>>()
    val errorFetchingFaqs = MutableLiveData<Event<Boolean>>()
    val loading = MutableLiveData<Boolean>()

    fun onTouClick(fragment: Fragment) {
        ExternalLinksHelper.openLink(
            fragment.requireContext(),
            settingsManager.termsOfUseUrl
        )
    }

    fun onFaqClick() {
        if (settingsManager.faqs.value.isNullOrEmpty()) {
            viewModelScope.launch {
                loading.value = true
                delay(1000)
                val faqs = withTimeoutOrNull(5000) {
                    settingsManager.fetchFaqsAsync().await()
                }
                loading.value = false
                if (faqs is FetchFaqsResult.Success) {
                    navigateToFaqs.value = Event(true)
                } else {
                    errorFetchingFaqs.value = Event(true)
                }
            }
        } else {
            navigateToFaqs.value = Event(true)
        }
    }

    fun openExposureSettings(fragment: SettingsFragment) {
        fragment.startActivityForResult(
            ExposureNotificationClient.exposureNotificationSettingsIntent,
            EXPOSURE_NOTIFICATION_SETTINGS_REQUEST
        )
    }
}
