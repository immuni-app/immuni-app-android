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
