package it.ministerodellasalute.immuni.logic.greencovidcertificate.model

data class CertificateModel(
    val person: PersonModel,
    val dateOfBirth: String,
    val vaccinations: List<VaccinationModel>?,
    val tests: List<TestModel>?,
    val recoveryStatements: List<RecoveryModel>?
)

data class PersonModel(
    val standardisedFamilyName: String,
    val familyName: String?,
    val standardisedGivenName: String?,
    val givenName: String?
)

data class VaccinationModel(
    override val disease: String,
    val vaccine: String,
    val medicinalProduct: String,
    val manufacturer: String,
    val doseNumber: Int,
    val totalSeriesOfDoses: Int,
    val dateOfVaccination: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String
) : CertificateData

data class TestModel(
    override val disease: String,
    val typeOfTest: String,
    val testName: String?,
    val testNameAndManufacturer: String?,
    val dateTimeOfCollection: String,
    val dateTimeOfTestResult: String?,
    val testResult: String,
    val testingCentre: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String,
    val resultType: TestResult
) : CertificateData

enum class TestResult(val value: String) {
    DETECTED("DETECTED"),
    NOT_DETECTED("NOT DETECTED")
}

data class RecoveryModel(
    override val disease: String,
    val dateOfFirstPositiveTest: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateValidFrom: String,
    val certificateValidUntil: String,
    val certificateIdentifier: String
) : CertificateData

interface CertificateData {
    val disease: String
}
