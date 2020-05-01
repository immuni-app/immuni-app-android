package org.immuni.android.networking

import org.immuni.android.extensions.utils.fromJson
import org.immuni.android.networking.api.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.immuni.android.networking.api.*
import java.lang.Exception
import kotlin.reflect.KClass

class NetworkingRepository<Settings : NetworkingSettings>(
    val api: NetworkingService,
    val store: NetworkingStore,
    private val settingsType: KClass<Settings>,
    private val settingsChannel: ConflatedBroadcastChannel<Settings>
) {
    suspend fun settings(): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.settings() }
    }

    // fetch the /settings from the server
    // store it in the local store
    // send it to all the listeners through the broadcast channel

    suspend fun fetchSettings(): NetworkResource<Settings, ErrorResponse> =
        withContext(Dispatchers.IO) {
            val baseResult = this@NetworkingRepository.settings()

            try {
                when(baseResult) {
                    is NetworkResource.Success -> {
                        val json = baseResult.data?.string() ?: "{}"
                        val result = fromJson(settingsType, json)

                        store.saveSettings(json)

                        result?.let {
                            settingsChannel.send(it)
                        }
                        NetworkResource.Success<Settings, ErrorResponse>(result as Settings)
                    }
                    is NetworkResource.Error -> {
                        NetworkResource.Error<Settings, ErrorResponse>(baseResult.error!!)
                    }
                    else -> NetworkResource.Error<Settings, ErrorResponse>(NetworkError.Unknown())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                NetworkResource.Error<Settings, ErrorResponse>(NetworkError.Unknown())
            }
        }
}
