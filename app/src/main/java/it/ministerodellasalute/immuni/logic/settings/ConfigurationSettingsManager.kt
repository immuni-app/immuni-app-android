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

package it.ministerodellasalute.immuni.logic.settings

import android.content.Context
import androidx.annotation.VisibleForTesting
import it.ministerodellasalute.immuni.api.services.ConfigurationSettings
import it.ministerodellasalute.immuni.api.services.Language
import it.ministerodellasalute.immuni.extensions.utils.DeviceUtils
import it.ministerodellasalute.immuni.extensions.utils.UserLocale
import it.ministerodellasalute.immuni.extensions.utils.log
import it.ministerodellasalute.immuni.logic.settings.models.FetchFaqsResult
import it.ministerodellasalute.immuni.logic.settings.models.FetchSettingsResult
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsNetworkRepository
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ConfigurationSettingsManager.
 *
 * It exposes the settings through a reactive Flow or sync methods.
 *
 */
class ConfigurationSettingsManager(
    private val networkRepository: ConfigurationSettingsNetworkRepository,
    private val storeRepository: ConfigurationSettingsStoreRepository,
    private val buildVersion: Long
) {
    constructor(
        context: Context,
        networkRepository: ConfigurationSettingsNetworkRepository,
        storeRepository: ConfigurationSettingsStoreRepository
    ) : this(
        networkRepository = networkRepository,
        storeRepository = storeRepository,
        buildVersion = DeviceUtils.appVersionCode(context)
    )

    private val _settings = MutableStateFlow(storeRepository.loadSettings())
    val settings: StateFlow<ConfigurationSettings> get() = _settings

    suspend fun fetchSettings(): FetchSettingsResult {
        val response = networkRepository.fetchSettings(buildVersion)
        if (response is FetchSettingsResult.Success) {
            onSettingsUpdate(response.settings)
        }

        return response
    }

    @VisibleForTesting
    fun onSettingsUpdate(settings: ConfigurationSettings) {
        storeRepository.saveSettings(settings)
        _settings.value = settings
    }

    val isAppOutdated: Boolean
        get() {
            val outdated = settings.value.minimumBuildVersion > buildVersion
            log("App outdated: $outdated")
            return outdated
        }

    suspend fun fetchFaqs(): FetchFaqsResult {
        val language = Language.fromCode(UserLocale.locale())
        val url: String = settings.value.faqUrl[language] ?: ""
        return networkRepository.fetchFaqs(url)
    }
}
