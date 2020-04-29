package org.immuni.android.networking

import okhttp3.ResponseBody
import org.immuni.android.networking.model.BtIds
import org.immuni.android.networking.model.FcmTokenRequest
import org.immuni.android.networking.model.ImmuniMe
import org.immuni.android.networking.model.ImmuniSettings
import org.immuni.android.models.ExportData
import org.koin.core.KoinComponent
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class ApiManager(
    val networking: Networking<ImmuniSettings, ImmuniMe>
) : KoinComponent {
    private val api = networking.customServiceAPI(CustomOracleAPI::class)

    suspend fun exportData(code: String, data: ExportData): Response<ResponseBody> {
        val result = api.exportData(code, data)
        if(result.isSuccessful) {
            me() // update me model
        }
        return result
    }

    suspend fun getBtIds() = api.getBtIds()

    suspend fun settings(): Response<ImmuniSettings> {
        return networking.api.fetchSettings()
    }

    suspend fun me(): Response<ImmuniMe> {
        return networking.api.fetchMe()
    }
}

interface CustomOracleAPI {
    @POST("v2/notifications/fcm")
    suspend fun fcmNotificationToken(@Body reedem: FcmTokenRequest): Response<ResponseBody>

    @GET("v2/bt")
    suspend fun getBtIds(): Response<BtIds>

    @POST("v3/data/{code}")
    suspend fun exportData(@Path("code") code: String, @Body data: ExportData): Response<ResponseBody>
}
