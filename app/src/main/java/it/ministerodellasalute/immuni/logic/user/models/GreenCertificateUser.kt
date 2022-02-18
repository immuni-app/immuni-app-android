package it.ministerodellasalute.immuni.logic.user.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dgca.verifier.app.decoder.model.GreenCertificate
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class GreenCertificateUser(
    @field:Json(name = "base64") val base64: String,
    @field:Json(name = "greenCertificate") val data: GreenCertificate?,
    @field:Json(name = "fglTipoDgc") val fglTipoDgc: String? = null
) : Parcelable
