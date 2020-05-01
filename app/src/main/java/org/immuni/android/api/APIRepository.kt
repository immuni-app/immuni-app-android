package org.immuni.android.api

import okhttp3.ResponseBody
import org.immuni.android.api.model.BtIds
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.models.ExportData
import org.immuni.android.network.Network
import org.immuni.android.api.model.ErrorResponse
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.network.api.safeApiCall
import org.koin.core.KoinComponent

/**
 * Interact with the [API] created using the [Network] module.
 */
class APIRepository(
    val network: Network,
    private val store: APIStore
) {
    private val api = network.createServiceAPI(API::class)

    private val listeners = mutableListOf<APIListener>()

    fun addAPIListener(listener: APIListener) {
        listeners.add(listener)
    }

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

    /**
     * Fetch settings, store them and notify listeners.
     */
    suspend fun settings(): NetworkResource<ImmuniSettings, ErrorResponse> {
        val resource = safeApiCall<ImmuniSettings, ErrorResponse> { api.settings() }

        if(resource is NetworkResource.Success) {
            resource.data?.let { settings ->
                store.saveSettings(settings)
                listeners.forEach {
                    it.onSettingsUpdate(settings)
                }
            }
        }

        return resource
    }
}