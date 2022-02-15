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

package it.ministerodellasalute.immuni.ui.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.ui.home.HomeFragmentDirections

class StateCloseViewModel(
    private val exposureManager: ExposureManager,
    val settingsManager: ConfigurationSettingsManager
) : ViewModel() {

    fun onPrivacyPolicyClick(fragment: StateCloseDialogFragment) {
        ExternalLinksHelper.openLink(
            fragment.requireContext(),
            settingsManager.privacyNoticeUrl
        )
    }

    fun onQuarantineIsolationClick(fragment: StateCloseDialogFragment) {
        val action = HomeFragmentDirections.actionFindOutMoreStateClose()
        fragment.findNavController().navigate(action)
    }

    val exposureDate = exposureManager.exposureStatus.asLiveData()
}
