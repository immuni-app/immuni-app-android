package org.immuni.android.api

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.immuni.android.ImmuniApplication
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.extensions.lifecycle.AppLifecycleEvent
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.ui.forceupdate.ForceUpdateActivity
import org.immuni.android.util.log

/**
 * Manages calls and data regarding the backend [API]
 *
 * @param repository responsible to interact with the [API] and API storage.
 */
class APIManager(
    val context: Context,
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

    fun settingsFlow(): Flow<ImmuniSettings> {
        return settingsChannel.asFlow()
    }

    override suspend fun onSettingsUpdate(settings: ImmuniSettings) {
        settingsChannel.send(settings)
    }
}