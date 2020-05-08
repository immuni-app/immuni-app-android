package org.immuni.android.api

import okhttp3.ResponseBody
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.api.model.ImmuniSettings
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * App Configuration Service API.
 */
interface AppConfigurationService {
    @GET("v1/settings/app")
    suspend fun settings(): Response<ImmuniSettings>

    @POST("v2/notifications/fcm")
    suspend fun fcmNotificationToken(@Body token: FcmTokenRequest): Response<ResponseBody>
}
