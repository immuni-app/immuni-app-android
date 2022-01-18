package dgca.verifier.app.decoder.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Exemption(
    @JsonProperty("fc")
    val fiscalCode: String,

    @JsonProperty("du")
    val certificateValidUntil: String,

    @JsonProperty("co")
    val countryOfExemption: String,

    @JsonProperty("ci")
    val certificateIdentifier: String,

    @JsonProperty("cu")
    val uniqueVaccinationExemptionIdentifier: String,

    @JsonProperty("is")
    val certificateIssuer: String,

    @JsonProperty("tg")
    val disease: String,

    @JsonProperty("df")
    val certificateValidFrom: String
) : Serializable
