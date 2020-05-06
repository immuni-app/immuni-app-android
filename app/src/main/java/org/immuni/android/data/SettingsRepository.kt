package org.immuni.android.data

import kotlinx.coroutines.flow.Flow
import org.immuni.android.api.AppConfigurationService
import org.immuni.android.api.model.ErrorResponse
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.network.api.safeApiCall

/**
 * Setting repository.
 */
class SettingsRepository(
    val appConfigurationService: AppConfigurationService,
    val dataSource: SettingsDataSource
) {
    suspend fun fetchSettings(): NetworkResource<ImmuniSettings, ErrorResponse> {
        return safeApiCall<ImmuniSettings, ErrorResponse> { appConfigurationService.settings() }
    }

    fun latestSettings(): ImmuniSettings? {
        return dataSource.latestSettings()
    }

    fun settingsFlow(): Flow<ImmuniSettings> {
        return dataSource.settingsFlow()
    }
}