package com.bendingspoons.oracle

import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.base.utils.toJson
import com.bendingspoons.oracle.api.OracleService
import com.bendingspoons.oracle.api.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.lang.Exception
import kotlin.reflect.KClass

class OracleRepository<Settings : OracleSettings, Me : OracleMe>(
    val api: OracleService,
    val store: OracleStore,
    private val settingsType: KClass<Settings>,
    private val meType: KClass<Me>,
    private val settingsChannel: ConflatedBroadcastChannel<Settings>,
    private val meChannel: ConflatedBroadcastChannel<Me>
) {
    suspend fun settings(): Response<ResponseBody> {
        return api.settings()
    }

    suspend fun forceSegment(request: ForceExperimentRequest): Response<ResponseBody> {
        return api.forceExperiment(request)
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

    suspend fun redeemGiftCode(request: RedeemGiftCodeRequest): Response<ResponseBody> {
        return api.redeemGiftCode((request))
    }

    suspend fun verifyPurchase(request: VerifyPurchaseRequest): Response<VerifyPurchaseResponse> {
        return api.verifyPurchase(request)
    }

    // fetch the /me from the server
    // store it in the local store
    // send it to all the listeners through the broadcast channel

    suspend fun fetchMe(): Response<Me> = withContext(Dispatchers.IO) {
        val baseResult: Response<ResponseBody> = this@OracleRepository.me()
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
            val baseResult: Response<ResponseBody> = this@OracleRepository.settings()
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

    suspend fun forceIsSubscribed(currentMe: Me, subscribed: Boolean = true) {
        withContext(Dispatchers.IO) {
            val meJson = toJson(meType, currentMe)
                .replace("is_subscribed:false", "is_subscribed:$subscribed")
                .replace("is_subscribed:true", "is_subscribed:$subscribed")
            val newMe = fromJson(meType, meJson)
            assert(newMe != null) { "Error deserializing me" }
            meChannel.send(newMe!!)
            store.saveMe(meJson)
        }
    }
}
