package org.immuni.android.analytics.api

import org.immuni.android.analytics.api.model.PicoEventRequest
import org.immuni.android.analytics.api.model.PicoEventResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PicoService {

    @POST("/v2/events")
    suspend fun event(@Body request: PicoEventRequest): Response<PicoEventResponse>
}

