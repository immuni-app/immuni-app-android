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

package it.ministerodellasalute.immuni.ui.otp

import android.os.Parcelable
import java.util.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OtpToken(val otp: String, val serverDate: Date) : Parcelable {
    companion object {
        fun fromLogic(token: it.ministerodellasalute.immuni.logic.exposure.models.OtpToken): OtpToken {
            return OtpToken(otp = token.otp, serverDate = token.serverDate)
        }
    }

    fun toLogic(): it.ministerodellasalute.immuni.logic.exposure.models.OtpToken {
        return it.ministerodellasalute.immuni.logic.exposure.models.OtpToken(otp = otp, serverDate = serverDate)
    }
}
