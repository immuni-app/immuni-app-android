/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
