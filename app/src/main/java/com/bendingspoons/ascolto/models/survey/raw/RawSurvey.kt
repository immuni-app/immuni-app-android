package com.bendingspoons.ascolto.models.survey.raw

import com.bendingspoons.ascolto.models.survey.Condition
import com.bendingspoons.ascolto.models.survey.HealthStatus
import com.bendingspoons.ascolto.models.survey.Survey
import com.bendingspoons.ascolto.models.survey.Triage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawSurvey(
    @field:Json(name = "version") val version: String,
    @field:Json(name = "questions") val questions: List<RawQuestion>,
    @field:Json(name = "triage") val triage: Map<RawHealthStatus, List<RawConditionItem>>
) {
    fun survey() = Survey(
        version = version,
        questions = questions.map { it.question() },
        triage = Triage(
            triage.mapKeys {
                it.key.healthStatus()
            }.mapValues {
                Condition(it.value.map { item -> item.conditionItem() })
            }
        )
    )
}

enum class RawHealthStatus {
    @Json(name = "covid_positive")
    COVID_POSITIVE,
    @Json(name = "serious_symptoms")
    SERIOUS_SYMPTOMS,
    @Json(name = "healthy_with_established_contact")
    HEALTHY_WITH_ESTABLISHED_CONTACT,
    @Json(name = "mild_symptoms_no_established_contact")
    MILD_SYMPTOMS_NO_ESTABLISHED_CONTACT,
    @Json(name = "healthy_no_established_contact")
    HEALTHY_NO_ESTABLISHED_CONTACT;

    fun healthStatus() = when (this) {
        COVID_POSITIVE -> HealthStatus.COVID_POSITIVE
        SERIOUS_SYMPTOMS -> HealthStatus.SERIOUS_SYMPTOMS
        HEALTHY_WITH_ESTABLISHED_CONTACT -> HealthStatus.HEALTHY_WITH_ESTABLISHED_CONTACT
        MILD_SYMPTOMS_NO_ESTABLISHED_CONTACT -> HealthStatus.MILD_SYMPTOMS_NO_ESTABLISHED_CONTACT
        HEALTHY_NO_ESTABLISHED_CONTACT -> HealthStatus.HEALTHY_NO_ESTABLISHED_CONTACT
    }
}
