package it.ministerodellasalute.immuni.ui.cun

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CunToken(val cun: String) : Parcelable {

    val CUN_CODE_LENGTH = 10

    companion object {
        fun fromLogic(token: it.ministerodellasalute.immuni.logic.exposure.models.CunToken): CunToken {
            return CunToken(cun = token.cun)
        }
    }

    fun toLogic(): it.ministerodellasalute.immuni.logic.exposure.models.CunToken {
        return it.ministerodellasalute.immuni.logic.exposure.models.CunToken(cun = cun)
    }
}
