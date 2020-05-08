package org.immuni.android.data

import okhttp3.ResponseBody
import org.immuni.android.api.AppConfigurationService
import org.immuni.android.api.model.ErrorResponse
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.network.api.safeApiCall

/**
 * Firebase Cloud Messaging repository.
 */
class FcmRepository(
    val appConfigurationService: AppConfigurationService
) {

    suspend fun fcmNotificationToken(request: FcmTokenRequest): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { appConfigurationService.fcmNotificationToken(request) }
    }
}
