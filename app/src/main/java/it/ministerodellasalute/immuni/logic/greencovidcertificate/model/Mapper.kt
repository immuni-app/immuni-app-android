package it.ministerodellasalute.immuni.logic.greencovidcertificate.model

import dgca.verifier.app.decoder.model.*

fun GreenCertificate.toCertificateModel(): CertificateModel {
    return CertificateModel(
        person.toPersonModel(),
        dateOfBirth,
        vaccinations?.map { it.toVaccinationModel() },
        tests?.map { it.toTestModel() },
        recoveryStatements?.map { it.toRecoveryModel() }
    )
}

fun RecoveryStatement.toRecoveryModel(): RecoveryModel {
    return RecoveryModel(
        disease,
        dateOfFirstPositiveTest,
        countryOfVaccination,
        certificateIssuer,
        certificateValidFrom,
        certificateValidUntil,
        certificateIdentifier
    )
}

fun Test.toTestModel(): TestModel {
    return TestModel(
        disease,
        typeOfTest,
        testName,
        testNameAndManufacturer,
        dateTimeOfCollection,
        dateTimeOfTestResult,
        testResult,
        testingCentre,
        countryOfVaccination,
        certificateIssuer,
        certificateIdentifier,
        getTestResultType().toTestResult()
    )
}

fun Test.TestResult.toTestResult(): TestResult {
    return when (this) {
        Test.TestResult.DETECTED -> TestResult.DETECTED
        Test.TestResult.NOT_DETECTED -> TestResult.NOT_DETECTED
    }
}

fun Vaccination.toVaccinationModel(): VaccinationModel {
    return VaccinationModel(
        disease,
        vaccine,
        medicinalProduct,
        manufacturer,
        doseNumber,
        totalSeriesOfDoses,
        dateOfVaccination,
        countryOfVaccination,
        certificateIssuer,
        certificateIdentifier
    )
}

fun Person.toPersonModel(): PersonModel {
    return PersonModel(
        standardisedFamilyName,
        familyName,
        standardisedGivenName,
        givenName
    )
}
