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
import it.ministerodellasalute.immuni.api.services.Faq
import it.ministerodellasalute.immuni.api.services.Language
import it.ministerodellasalute.immuni.extensions.utils.DeviceUtils
import it.ministerodellasalute.immuni.extensions.utils.UserLocale
import it.ministerodellasalute.immuni.extensions.utils.log
import it.ministerodellasalute.immuni.logic.settings.models.FetchFaqsResult
import it.ministerodellasalute.immuni.logic.settings.models.FetchSettingsResult
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsNetworkRepository
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsStoreRepository
import kotlinx.coroutines.*
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

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    // region: ConfigurationSettings

    private val _settings = MutableStateFlow(storeRepository.loadSettings())
    val settings: StateFlow<ConfigurationSettings> get() = _settings

    fun fetchSettingsAsync(): Deferred<FetchSettingsResult> {
        return scope.async {
            val result = networkRepository.fetchSettings(buildVersion)
            if (result is FetchSettingsResult.Success) {
                onSettingsUpdate(result.settings)
            }
            result
        }
    }

    @VisibleForTesting
    fun onSettingsUpdate(settings: ConfigurationSettings) {
        storeRepository.saveSettings(settings)
        _settings.value = settings
        fetchFaqsAsync()
    }

    val isAppOutdated: Boolean
        get() {
            val isOutdated = settings.value.minimumBuildVersion > buildVersion
            log("App outdated: $isOutdated")
            return isOutdated
        }

    // endregion

    // region: FAQs

    private val currentLanguage get() = Language.fromCode(UserLocale.locale())
    private var _faqLanguage = currentLanguage

    private val _faqs = MutableStateFlow(storeRepository.loadFaqs(_faqLanguage))
    val faqs: StateFlow<List<Faq>?>
        get() {
            if (currentLanguage != _faqLanguage) {
                _faqLanguage = currentLanguage
                _faqs.value = storeRepository.loadFaqs(_faqLanguage)
            }
            return _faqs
        }

    fun fetchFaqsAsync(): Deferred<FetchFaqsResult> {
        return scope.async {
            val language = if (settings.value.faqUrls.containsKey(currentLanguage.code)) currentLanguage else Language.EN
            val url = settings.value.faqUrls[language.code] ?: return@async FetchFaqsResult.ServerError
            val result = networkRepository.fetchFaqs(url)
            if (result is FetchFaqsResult.Success) {
                onFaqsUpdate(language, result.faqs)
            }
            result
        }
    }

    @VisibleForTesting
    fun onFaqsUpdate(language: Language, faqs: List<Faq>) {
        storeRepository.saveFaqs(language, faqs)
        _faqs.value = faqs
    }

    // endregion

    val privacyNoticeUrl: String
        get() {
            val privacyNoticeUrls = settings.value.privacyNoticeUrls
            return privacyNoticeUrls[currentLanguage.code] ?: privacyNoticeUrls[Language.EN.code] ?: privacyNoticeUrls[Language.IT.code] ?: error("Missing Privacy Notice URL")
        }

    val termsOfUseUrl: String
        get() {
            val termsOfUseUrls = settings.value.termsOfUseUrls
            return termsOfUseUrls[currentLanguage.code] ?: termsOfUseUrls[Language.EN.code] ?: termsOfUseUrls[Language.IT.code] ?: error("Missing Terms of Use URL")
        }
}
