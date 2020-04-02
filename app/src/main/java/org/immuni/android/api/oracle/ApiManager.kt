package org.immuni.android.api.oracle

import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.oracle.Oracle
import okhttp3.ResponseBody
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.api.oracle.model.BtIds
import org.immuni.android.api.oracle.model.FcmTokenRequest
import org.immuni.android.models.ExportData
import org.immuni.android.models.User
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import retrofit2.http.*

class ApiManager : KoinComponent {
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val api = oracle.customServiceAPI(CustomOracleAPI::class)

    private inline fun <reified T: Any> decode(response: Response<ResponseBody>): T? {
        if (response.isSuccessful) {
            response.body()?.string()?.let { json ->
                return fromJson(json)
            }
        }
        return null
    }

    private suspend fun updateMe(response: Response<ResponseBody>): ImmuniMe? {
        return decode<ImmuniMe>(response)?.also {
            oracle.updateMe(it)
        }
    }

    suspend fun updateMainUser(user: User) = updateMe(api.updateMainUser(user))

    suspend fun createFamilyMember(user: User) = updateMe(api.createFamilyMember(user))

    suspend fun updateExistingFamilyMember(userId: String, user: User) = updateMe(api.updateExistingFamilyMember(userId, user))

    suspend fun deleteFamilyMember(userId: String) = updateMe(api.deleteFamilyMember(userId))

    suspend fun exportData(code: String, data: ExportData): Response<ResponseBody> {
        return api.exportData(code, data)
    }

    suspend fun getBtIds() = api.getBtIds()
}

interface CustomOracleAPI {
    @POST("notifications/fcm")
    suspend fun fcmNotificationToken(@Body reedem: FcmTokenRequest): Response<ResponseBody>

    @GET("bt")
    suspend fun getBtIds(): Response<BtIds>

    @POST("householder")
    suspend fun updateMainUser(@Body user: User): Response<ResponseBody>

    @POST("relatives")
    suspend fun createFamilyMember(@Body user: User): Response<ResponseBody>

    @PUT("relatives/{userId}")
    suspend fun updateExistingFamilyMember(@Path("userId") userId: String, @Body user: User): Response<ResponseBody>

    @DELETE("relatives/{userId}")
    suspend fun deleteFamilyMember(@Path("userId") userId: String): Response<ResponseBody>

    @POST("data/{code}")
    suspend fun exportData(@Path("code") code: String, @Body data: ExportData): Response<ResponseBody>
}
