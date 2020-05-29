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
import androidx.lifecycle.ViewModel
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.util.startSendingEmail
import org.koin.core.KoinComponent

class SettingsViewModel(
    private val settingsManager: ConfigurationSettingsManager
) : ViewModel(), KoinComponent {
    companion object {
        const val EXPOSURE_NOTIFICATION_SETTINGS_REQUEST = 2206
    }

    private val settings get() = settingsManager.settings.value

    fun onTouClick(fragment: Fragment) {
        ExternalLinksHelper.openLink(
            fragment.requireContext(),
            settingsManager.termsOfUseUrl
        )
    }

    fun onSupportClick(fragment: Fragment) {
        val email = settings.supportEmail

        fragment.startSendingEmail(
            email,
            fragment.getString(R.string.app_name),
            "",
            fragment.getString(R.string.settings_setting_contact_support)
        )
    }

    fun openExposureSettings(fragment: SettingsFragment) {
        fragment.startActivityForResult(
            ExposureNotificationClient.exposureNotificationSettingsIntent,
            EXPOSURE_NOTIFICATION_SETTINGS_REQUEST
        )
    }
}
