package org.immuni.android.api

import okhttp3.ResponseBody
import org.immuni.android.api.model.BtIds
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.models.ExportData
import org.immuni.android.network.Network
import org.immuni.android.api.model.ErrorResponse
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.data.SettingsStore
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.network.api.safeApiCall

/**
 * TODO split in dedicated repositories.
 */
class TODOAPIRepository(
    val api: API
) {

    suspend fun exportData(
        code: String,
        data: ExportData
    ): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.exportData(code, data) }
    }

    suspend fun getBtIds(): NetworkResource<BtIds, ErrorResponse> {
        return safeApiCall { api.getBtIds() }
    }

    suspend fun fcmNotificationToken(request: FcmTokenRequest): NetworkResource<ResponseBody, ErrorResponse> {
        return safeApiCall { api.fcmNotificationToken(request) }
    }
}