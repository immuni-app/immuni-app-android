package com.bendingspoons.pico.api

import com.bendingspoons.pico.api.model.PicoEventRequest
import com.bendingspoons.pico.api.model.PicoEventResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PicoService {

    @POST("/v2/events")
    suspend fun event(@Body request: PicoEventRequest): Response<PicoEventResponse>
}

