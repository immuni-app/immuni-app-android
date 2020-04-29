package org.immuni.android.api

import okhttp3.ResponseBody
import org.immuni.android.api.model.BtIds
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.models.ExportData
import org.immuni.android.networking.Networking
import org.koin.core.KoinComponent
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class ImmuniAPIRepository(
    val networking: Networking<ImmuniSettings, ImmuniMe>
) : KoinComponent {
    private val api = networking.customServiceAPI(ImmuniAPI::class)

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

interface ImmuniAPI {
    @POST("v2/notifications/fcm")
    suspend fun fcmNotificationToken(@Body reedem: FcmTokenRequest): Response<ResponseBody>

    @GET("v2/bt")
    suspend fun getBtIds(): Response<BtIds>

    @POST("v3/data/{code}")
    suspend fun exportData(@Path("code") code: String, @Body data: ExportData): Response<ResponseBody>
}
