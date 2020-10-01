package it.ministerodellasalute.immuni.logic.settings.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import it.ministerodellasalute.immuni.api.services.ExposureIngestionService

@JsonClass(generateAdapter = true)
data class Country (
    @field:Json(name = "country") val country: ExposureIngestionService.Country?
)

@JsonClass(generateAdapter = true)
data class CountryOb (
    val listCountries : MutableList<Country> = mutableListOf()
)
