package it.ministerodellasalute.immuni.logic.user.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class GreenCertificateUser(
    @field:Json(name = "base64") val base64: String,
    @field:Json(name = "nameSurname") val nameSurname: String = "-",
    @field:Json(name = "birthDate") val birthDate: String = "-",
    @field:Json(name = "certificateID") val certificateID: String = "-",
    @field:Json(name = "targetAgent") val targetAgent: String = "-",
    @field:Json(name = "typeOfVaccine") val typeOfVaccine: String = "-",
    @field:Json(name = "vaccineName") val vaccineName: String = "-",
    @field:Json(name = "vaccineProducer") val vaccineProducer: String = "-",
    @field:Json(name = "dosesNumber") val dosesNumber: String = "-",
    @field:Json(name = "totalDosesNumber") val totalDosesNumber: String = "-",
    @field:Json(name = "dateOfLastVaccination") val dateOfLastVaccination: String = "-",
    @field:Json(name = "perfomedCountryVaccination") val perfomedCountryVaccination: String = "-",
    @field:Json(name = "entityIssuedCertificate") val entityIssuedCertificate: String = "-"
) : Parcelable
