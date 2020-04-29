package org.immuni.android.networking

import org.immuni.android.base.utils.fromJson
import org.immuni.android.networking.api.NetworkingService
import org.immuni.android.networking.api.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
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
    suspend fun settings(): Response<ResponseBody> {
        return api.settings()
    }

    suspend fun me(): Response<ResponseBody> {
        return api.me()
    }

    suspend fun devices(request: DevicesRequest): Response<ResponseBody> {
        return api.devices(request)
    }

    suspend fun privacyNotice(request: PrivacyNoticeRequest): Response<ResponseBody> {
        return api.privacyNotice(request)
    }

    // fetch the /me from the server
    // store it in the local store
    // send it to all the listeners through the broadcast channel

    suspend fun fetchMe(): Response<Me> = withContext(Dispatchers.IO) {
        val baseResult: Response<ResponseBody> = this@NetworkingRepository.me()
        val httpCode = baseResult.code()

        try {
            if (baseResult.isSuccessful) {
                val json = baseResult.body()?.string() ?: "{}"
                val result = fromJson(meType, json)

                store.saveMe(json)

                result?.let {
                    meChannel.send(it)
                }
                Response.success(httpCode, result)
            } else {
                Response.error(httpCode, baseResult.errorBody() ?: "".toResponseBody())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Response.error<Me>(httpCode, baseResult.errorBody() ?: "".toResponseBody())
        }
    }

    // fetch the /settings from the server
    // store it in the local store
    // send it to all the listeners through the broadcast channel

    suspend fun fetchSettings(): Response<Settings> =
        withContext(Dispatchers.IO) {
            val baseResult: Response<ResponseBody> = this@NetworkingRepository.settings()
            val httpCode = baseResult.code()

            try {
                if (baseResult.isSuccessful) {
                    val json = baseResult.body()?.string() ?: "{}"
                    val result = fromJson(settingsType, json)

                    store.saveSettings(json)

                    result?.let {
                        settingsChannel.send(it)
                    }
                    Response.success(httpCode, result)
                } else {
                    Response.error(httpCode, baseResult.errorBody() ?: "".toResponseBody())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Response.error<Settings>(httpCode, baseResult.errorBody() ?: "".toResponseBody())
            }
        }
}
