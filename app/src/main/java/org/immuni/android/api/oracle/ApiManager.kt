package org.immuni.android.api.oracle

import com.bendingspoons.oracle.Oracle
import okhttp3.ResponseBody
import org.immuni.android.api.oracle.model.BtIds
import org.immuni.android.api.oracle.model.FcmTokenRequest
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.models.ExportData
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class ApiManager : KoinComponent {
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val api = oracle.customServiceAPI(CustomOracleAPI::class)

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

    @POST("data/{code}")
    suspend fun exportData(@Path("code") code: String, @Body data: ExportData): Response<ResponseBody>
}
