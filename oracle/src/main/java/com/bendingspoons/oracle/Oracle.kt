package com.bendingspoons.oracle

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.bendingspoons.base.lifecycle.AppLifecycleEvent.*
import com.bendingspoons.base.lifecycle.AppLifecycleObserver
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.base.utils.toJson
import com.bendingspoons.oracle.api.OracleRetrofit
import com.bendingspoons.oracle.api.OracleService
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.util.*
import kotlin.reflect.KClass
import kotlin.concurrent.schedule

inline fun <reified Settings : OracleSettings, reified Me : OracleMe> Oracle(
    context: Context,
    config: OracleConfiguration
) = Oracle(context, config, Settings::class, Me::class)

class Oracle<Settings : OracleSettings, Me : OracleMe>(
    private val context: Context,
    private val config: OracleConfiguration,
    private val settingsType: KClass<Settings>,
    private val meType: KClass<Me>
) {

    // OracleService contains the generic API all app can use.
    // If your app need to use a legacy API, use the customServiceAPI() method to
    // create your custom API above the Oracle layer.

    private val store = OracleStore(context, encrypted = config.encryptStore())
    private val settingsChannel: ConflatedBroadcastChannel<Settings>
    private val meChannel: ConflatedBroadcastChannel<Me>

    val api: OracleRepository<Settings, Me>

    private val lifecycleObserver: AppLifecycleObserver

    init {
        settingsChannel = loadSettings()
        meChannel = loadMe()

        val serverApi = OracleRetrofit(
            context,
            config
        ).oracleRetrofit.create(OracleService::class.java)

        api = OracleRepository(
            serverApi,
            store,
            settingsType,
            meType,
            settingsChannel,
            meChannel
        )

        lifecycleObserver = fetchSettingsAndMeOnStartEvent()

        checkMinBuildVersion()
    }

    private fun fetchSettingsAndMeOnStartEvent(): AppLifecycleObserver {
        val lifecycleObserver = AppLifecycleObserver()
        GlobalScope.launch {
            lifecycleObserver.consumeEach { event ->
                when (event) {
                    ON_START -> {
                        api.fetchSettings()
                        api.fetchMe()
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

    private fun <T : Any> load(objType: KClass<T>, serialized: String?): ConflatedBroadcastChannel<T> {
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

    private fun loadMe() = load(meType, store.loadMe())

    // other libs and the app can explicitly ask for the latest settings received
    fun settings() = settingsChannel.valueOrNull

    // other libs and the app can explicitly ask for the latest me received
    fun me() = meChannel.valueOrNull

    fun settingsFlow(): Flow<Settings> {
        return settingsChannel.asFlow()
    }

    fun meFlow(): Flow<Me> {
        return meChannel.asFlow()
    }

    suspend fun updateMe(me: Me) {
        store.saveMe(toJson(meType, me))
        meChannel.send(me)
    }

    // use this method to create an app specific layer of API above the Oracle generic one.

    fun <T : Any> customServiceAPI(apiClass: KClass<T>): T {
        return OracleRetrofit(context, config).oracleRetrofit.create(
            apiClass.java
        )
    }
}
