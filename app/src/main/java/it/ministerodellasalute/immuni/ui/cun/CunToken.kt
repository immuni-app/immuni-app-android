package it.ministerodellasalute.immuni.ui.cun

import android.os.Parcelable
import java.util.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CunToken(val cun: String, val serverDate: Date?) : Parcelable {

    companion object {
        fun fromLogic(cun: it.ministerodellasalute.immuni.logic.exposure.models.CunToken): CunToken {
            return CunToken(cun = cun.cun, serverDate = cun.serverDate)
        }
    }

    fun toLogic(): it.ministerodellasalute.immuni.logic.exposure.models.CunToken {
        return it.ministerodellasalute.immuni.logic.exposure.models.CunToken(cun = cun, serverDate = serverDate)
    }
}
