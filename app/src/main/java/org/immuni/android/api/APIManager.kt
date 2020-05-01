package org.immuni.android.api

import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.extensions.lifecycle.AppLifecycleEvent
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver

/**
 * Manages calls and data regarding the backend [API]
 *
 * @param repository responsible to interact with the [API] and API storage.
 */
class APIManager(
    val repository: APIRepository,
    val store: APIStore
): APIListener {
    private val settingsChannel: ConflatedBroadcastChannel<ImmuniSettings>
    private val lifecycleObserver: AppLifecycleObserver

    init {
        settingsChannel = loadSettings()
        lifecycleObserver = fetchSettingsOnStartEvent()
        repository.addAPIListener(this)
    }

    private fun fetchSettingsOnStartEvent(): AppLifecycleObserver {
        val lifecycleObserver = AppLifecycleObserver()
        GlobalScope.launch {
            lifecycleObserver.consumeEach { event ->
                when (event) {
                    AppLifecycleEvent.ON_START -> {
                        repository.settings()
                    }
                    else -> {
                    }
                }
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        return lifecycleObserver
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

    override suspend fun onSettingsUpdate(settings: ImmuniSettings) {
        settingsChannel.send(settings)
    }
}