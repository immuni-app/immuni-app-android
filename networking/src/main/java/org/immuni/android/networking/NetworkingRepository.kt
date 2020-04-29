package org.immuni.android.networking

import org.immuni.android.base.utils.fromJson
import org.immuni.android.networking.api.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.immuni.android.networking.api.*
import retrofit2.Response
import java.lang.Exception
import kotlin.reflect.KClass

class NetworkingRepository<Settings : NetworkingSettings, Me : NetworkingMe>(
    val api: NetworkingService,
    val store: NetworkingStore,
    private val settingsType: KClass<Settings>,
    private val meType: KClass<Me>,
    private val settingsChannel: ConflatedBroadcastChannel<Settings>,
    private val meChannel: ConflatedBroadcastChannel<Me>
) {
    suspend fun settings(): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.settings() }
    }

    suspend fun me(): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.me() }
    }

    suspend fun devices(request: DevicesRequest): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.devices(request) }
    }

    suspend fun privacyNotice(request: PrivacyNoticeRequest): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.privacyNotice(request) }
    }

    // fetch the /me from the server
    // store it in the local store
    // send it to all the listeners through the broadcast channel

    suspend fun fetchMe(): NetworkResource<Me, ErrorResponse> =
            withContext(Dispatchers.IO) {
                val baseResult = this@NetworkingRepository.me()

                try {
                    when(baseResult) {
                        is NetworkResource.Success -> {
                            val json = baseResult.data?.string() ?: "{}"
                            val result = fromJson(meType, json)

                            store.saveMe(json)

                            result?.let {
                                meChannel.send(it)
                            }
                            NetworkResource.Success<Me, ErrorResponse>(result as Me)
                        }
                        is NetworkResource.Error -> {
                            NetworkResource.Error<Me, ErrorResponse>(baseResult.error!!)
                        }
                        else -> NetworkResource.Error<Me, ErrorResponse>(NetworkError.Unknown())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    NetworkResource.Error<Me, ErrorResponse>(NetworkError.Unknown())
                }
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
