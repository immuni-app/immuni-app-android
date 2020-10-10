package it.ministerodellasalute.immuni.logic.exposure.models

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class CountryOfInterest(
    var code: String,
    var fullName: String,
    var insertDate: Date?,
    var lastProcessedChunk: Int = 0
)
