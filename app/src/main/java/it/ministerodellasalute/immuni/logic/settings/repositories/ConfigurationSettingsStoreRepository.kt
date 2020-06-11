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

package it.ministerodellasalute.immuni.logic.settings.repositories

import android.content.Context
import com.squareup.moshi.JsonClass
import it.ministerodellasalute.immuni.api.services.ConfigurationSettings
import it.ministerodellasalute.immuni.api.services.Faq
import it.ministerodellasalute.immuni.api.services.Language
import it.ministerodellasalute.immuni.extensions.storage.KVStorage

/**
 * Stores and loads the settings.
 */
class ConfigurationSettingsStoreRepository(
    private val context: Context,
    private val kvStorage: KVStorage,
    private val defaultSettings: ConfigurationSettings
) {
    @JsonClass(generateAdapter = true)
    data class Faqs(val faqs: Map<Language, List<Faq>>)

    companion object {
        val settingsKey = KVStorage.Key<ConfigurationSettings>("Settings")
        val faqsKey = KVStorage.Key<Faqs>("Faqs")
    }

    fun saveSettings(settings: ConfigurationSettings) = synchronized(this) {
        kvStorage[settingsKey] = settings
    }

    fun loadSettings(): ConfigurationSettings = synchronized(this) {
        // in case the ConfigurationSettings model has changed wrt the stored one,
        // we delete it and call loadSettings again to return the default ones
        return try {
            kvStorage[settingsKey, defaultSettings]
        } catch (e: Exception) {
            kvStorage.delete(settingsKey)
            loadSettings()
        }
    }

    fun saveFaqs(language: Language, faq: List<Faq>) = synchronized(this) {
        val faqs = kvStorage[faqsKey]
        kvStorage[faqsKey] = faqs?.copy(
            faqs = mutableMapOf<Language, List<Faq>>().apply {
                putAll(faqs.faqs)
                put(language, faq)
            }
        ) ?: Faqs(faqs = mapOf(language to faq))
    }

    fun loadFaqs(language: Language): List<Faq>? = synchronized(this) {
        // in case the Faq model has changed wrt the stored one,
        // we delete it and call loadFaqs again to return the default ones
        return try {
            kvStorage[faqsKey]?.faqs?.get(language)
        } catch (e: Exception) {
            if (kvStorage.contains(faqsKey)) {
                kvStorage.delete(faqsKey)
                loadFaqs(language)
            } else {
                error("Faqs corrupted")
            }
        }
    }
}
