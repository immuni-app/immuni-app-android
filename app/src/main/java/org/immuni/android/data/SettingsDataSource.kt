package org.immuni.android.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import org.immuni.android.api.AppConfigurationService
import org.immuni.android.api.model.ErrorResponse
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.extensions.lifecycle.AppLifecycleEvent
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.network.api.safeApiCall

/**
 * Settings data source.
 *
 * It exposes the settings through a reactive Flow or sync methods.
 *
 * @param repository responsible to interact with the [AppConfigurationService] and API storage.
 */
class SettingsDataSource(
    val appConfigurationService: AppConfigurationService,
    val store: SettingsStore,
    val lifecycleObserver: AppLifecycleObserver
) {
    private val settingsChannel: ConflatedBroadcastChannel<ImmuniSettings>

    init {
        settingsChannel = loadSettings()
        fetchSettingsOnStartEvent()
    }

    private fun fetchSettingsOnStartEvent() {
        GlobalScope.launch {
            lifecycleObserver.consumeEach { event ->
                when (event) {
                    AppLifecycleEvent.ON_START -> {
                        val resource = safeApiCall<ImmuniSettings, ErrorResponse> { appConfigurationService.settings() }
                        if(resource is NetworkResource.Success) {
                            resource.data?.let { settings ->
                                onSettingsUpdate(settings)
                            }
                        }
                    }
                    else -> {
                        // nothing to do here
                    }
                }
            }
        }
    }

    private fun loadSettings(): ConflatedBroadcastChannel<ImmuniSettings> {
        return when (val settings = store.loadSettings()) {
            null -> ConflatedBroadcastChannel()
            else -> ConflatedBroadcastChannel(settings)
        }
    }

    // other libs and the app can explicitly ask for the latest settings received
    fun latestSettings() = settingsChannel.valueOrNull

    fun closeSettingsChannel() {
        settingsChannel.cancel()
    }

    fun settingsFlow(): Flow<ImmuniSettings> {
        return settingsChannel.asFlow()
    }

    suspend fun onSettingsUpdate(settings: ImmuniSettings) {
        store.saveSettings(settings)
        settingsChannel.send(settings)
    }
}