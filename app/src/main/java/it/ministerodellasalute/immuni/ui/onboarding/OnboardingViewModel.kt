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

package it.ministerodellasalute.immuni.ui.onboarding

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.toast
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.extensions.notifications.PushNotificationManager
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.Province
import it.ministerodellasalute.immuni.logic.user.models.Region
import it.ministerodellasalute.immuni.logic.user.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class OnboardingViewModel(
    val handle: SavedStateHandle,
    val settingsManager: ConfigurationSettingsManager,
    val userManager: UserManager,
    val exposureManager: ExposureManager,
    val pushNotificationManager: PushNotificationManager
) : ViewModel(), KoinComponent {

    private val settings get() = settingsManager.settings.value

    val loading = MutableLiveData<Boolean>()

    private val _province = MutableStateFlow<Province?>(null)
    val province: StateFlow<Province?> = _province
    private val _region = MutableStateFlow<Region?>(null)
    val region: StateFlow<Region?> = _region

    val regions by lazy {
        userManager.regions()
    }

    val provinces: Flow<List<Province>> = region.map { region ->
        region?.let { userManager.provinces(it) } ?: listOf()
    }

    val navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val skipNextPage = MutableLiveData<Event<Boolean>>()
    val isBroadcastingActive = exposureManager.isBroadcastingActive.asLiveData()
    val navigateToPrevPage = MutableLiveData<Event<Boolean>>()

    init {
        userManager.user.value?.region?.let {
            _region.value = it
        }
    }

    fun onPrivacyPolicyClick(fragment: Fragment) {
        ExternalLinksHelper.openLink(
            fragment.requireContext(),
            settings.privacyPolicyUrl
        )
    }

    fun onTosClick(fragment: Fragment) {
        ExternalLinksHelper.openLink(
            fragment.requireContext(),
            settings.termsOfServiceUrl
        )
    }

    val isOnboardingComplete
        get() = userManager.isOnboardingComplete.value

    fun completeOnboarding() {
        userManager.setOnboardingComplete(true)
    }

    fun onEnterDonePage() {
        viewModelScope.launch {
            delay(2000)
            navigateToMainPage.value = Event(true)
        }
    }

    fun onRegionSelected(region: Region) {
        _region.value = region
        saveUserIfNeeded()
    }

    fun onProvinceSelected(province: Province) {
        _province.value = province
        saveUserIfNeeded()
    }

    private fun saveUserIfNeeded() {
        val region = region.value
        val province = province.value
        if (region != null && province != null) {
            userManager.save(
                User(
                    region = region,
                    province = province
                )
            )
        }
    }

    fun onNextTap() {
        navigateToNextPage.value = Event(true)
    }

    // If this region has only one province, skip the province selection page
    // And automatically select this province
    fun onRegionNextTap() {
        val provinces = _region.value?.provinces()
        val provincesCount = provinces?.size ?: Int.MAX_VALUE
        if (provinces != null && provincesCount == 1) {
            onProvinceSelected(provinces.first())
            skipNextPage.value = Event(true)
        } else {
            onNextTap()
        }
    }

    fun onPrevTap() {
        navigateToPrevPage.value = Event(true)
    }

    fun onPrivacyPolicyAccepted() {
        navigateToNextPage.value = Event(true)
    }

    fun startExposureNotification(activity: Activity) {
        viewModelScope.launch {
            try {
                exposureManager.optInAndStartExposureTracing(activity)
            } catch (e: Exception) {
                toast(
                    activity.applicationContext,
                    activity.getString(R.string.onboarding_exposure_api_not_activated)
                )
                e.printStackTrace()
            }
        }
    }
}
