package org.immuni.android.networking

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import org.immuni.android.extensions.lifecycle.AppLifecycleEvent.*
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.extensions.utils.fromJson
import org.immuni.android.extensions.utils.toJson
import org.immuni.android.networking.api.NetworkingRetrofit
import org.immuni.android.networking.api.NetworkingService
import org.immuni.android.networking.api.model.NetworkingSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlin.reflect.KClass

inline fun <reified Settings : NetworkingSettings> Networking(
    context: Context,
    config: NetworkingConfiguration
) = Networking(
    context,
    config,
    Settings::class
)

class Networking<Settings : NetworkingSettings>(
    private val context: Context,
    private val config: NetworkingConfiguration,
    private val settingsType: KClass<Settings>
) {

    private val store = NetworkingStore(
        context,
        encrypted = config.encryptStore()
    )
    private val settingsChannel: ConflatedBroadcastChannel<Settings>

    val api: NetworkingRepository<Settings>

    private val lifecycleObserver: AppLifecycleObserver

    init {
        settingsChannel = loadSettings()

        val serverApi = NetworkingRetrofit(
            context,
            config
        ).retrofit.create(NetworkingService::class.java)

        api = NetworkingRepository(
            serverApi,
            store,
            settingsType,
            settingsChannel
        )

        lifecycleObserver = fetchSettingsOnStartEvent()

        checkMinBuildVersion()
    }

    private fun fetchSettingsOnStartEvent(): AppLifecycleObserver {
        val lifecycleObserver = AppLifecycleObserver()
        GlobalScope.launch {
            lifecycleObserver.consumeEach { event ->
                when (event) {
                    ON_START -> {
                        api.fetchSettings()
                    }
                    else -> {
                    }
                }
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        return lifecycleObserver
    }

    private fun checkMinBuildVersion() {
        GlobalScope.launch {
            settingsChannel.consumeEach { settings ->
                if (settings.minBuildVersion > DeviceUtils.appVersionCode(context)) {
                    withContext(Dispatchers.Main) {
                        config.showForceUpdate(settings.minBuildVersion)
                    }
                }
            }
        }
    }

    private fun <T : Any> load(
        objType: KClass<T>,
        serialized: String?
    ): ConflatedBroadcastChannel<T> {
        val obj: T? = when (serialized) {
            null -> null
            else -> fromJson(objType, serialized)
        }
        return when (obj) {
            null -> ConflatedBroadcastChannel()
            else -> ConflatedBroadcastChannel(obj)
        }
    }

    private fun loadSettings() = load(settingsType, store.loadSettings())

    // other libs and the app can explicitly ask for the latest settings received
    fun settings() = settingsChannel.valueOrNull

    fun settingsFlow(): Flow<Settings> {
        return settingsChannel.asFlow()
    }

    // use this method to create an app specific layer of API above the generic one.
    fun <T : Any> customServiceAPI(apiClass: KClass<T>): T {
        return NetworkingRetrofit(context, config).retrofit.create(
            apiClass.java
        )
    }
}
