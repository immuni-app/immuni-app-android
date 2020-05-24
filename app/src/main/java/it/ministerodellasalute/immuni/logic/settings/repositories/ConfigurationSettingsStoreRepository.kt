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

import it.ministerodellasalute.immuni.api.services.ConfigurationSettings
import it.ministerodellasalute.immuni.extensions.storage.KVStorage

/**
 * Stores and loads the settings.
 */
class ConfigurationSettingsStoreRepository(
    private val kvStorage: KVStorage,
    private val defaultSettings: ConfigurationSettings
) {
    companion object {
        val settingsKey = KVStorage.Key<ConfigurationSettings>("Settings")
    }

    fun saveSettings(settings: ConfigurationSettings) {
        kvStorage[settingsKey] = settings
    }

    fun loadSettings(): ConfigurationSettings {
        // we handle the case settings model is changed
        // in that case we restart from the defaults.
        return try {
            kvStorage[settingsKey, defaultSettings]
        } catch (e: Exception) {
            kvStorage.delete(settingsKey)
            loadSettings()
        }
    }
}
