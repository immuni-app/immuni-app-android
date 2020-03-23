package org.ascolto.onlus.geocrowd19.android.api.oracle

import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.oracle.Oracle
import okhttp3.ResponseBody
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.FcmTokenRequest
import org.ascolto.onlus.geocrowd19.android.models.User
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

class ApiManager : KoinComponent {
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val api = oracle.customServiceAPI(CustomOracleAPI::class)

    private inline fun <reified T: Any> decode(response: Response<ResponseBody>): T? {
        if (response.isSuccessful) {
            response.body()?.string()?.let { json ->
                return fromJson(json)
            }
        }
        return null
    }

    private suspend fun updateMe(response: Response<ResponseBody>): AscoltoMe? {
        return decode<AscoltoMe>(response)?.also {
            it.mainUser?.isMain = true
            oracle.updateMe(it)
        }
    }

    suspend fun updateMainUser(user: User) = updateMe(api.updateMainUser(user))

    suspend fun createFamilyMember(user: User) = updateMe(api.createFamilyMember(user))

    suspend fun updateExistingFamilyMember(userId: String, user: User) = updateMe(api.updateExistingFamilyMember(userId, user))
}

interface CustomOracleAPI {
    @POST("notifications/fcm")
    suspend fun fcmNotificationToken(@Body reedem: FcmTokenRequest): Response<ResponseBody>

    @POST("householder")
    suspend fun updateMainUser(@Body user: User): Response<ResponseBody>

    @POST("relatives")
    suspend fun createFamilyMember(@Body user: User): Response<ResponseBody>

    @PUT("relatives/{userId}")
    suspend fun updateExistingFamilyMember(@Path("userId") userId: String, @Body user: User): Response<ResponseBody>
}
