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

package it.ministerodellasalute.immuni.logic.exposure.models

import com.squareup.moshi.JsonClass
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import it.ministerodellasalute.immuni.extensions.utils.DateUtils.MILLIS_IN_A_DAY
import java.util.*

sealed class ExposureStatus {
    companion object {
        val moshiAdapter: PolymorphicJsonAdapterFactory<ExposureStatus> =
            PolymorphicJsonAdapterFactory.of(ExposureStatus::class.java, "type")
                .withSubtype(None::class.java, "None")
                .withSubtype(Exposed::class.java, "Close")
                .withSubtype(Positive::class.java, "Positive")
    }

    @JsonClass(generateAdapter = true)
    data class None(@Transient private val foo: Int = 0) : ExposureStatus()

    @JsonClass(generateAdapter = true)
    data class Exposed(val lastExposureDate: Date, val acknowledged: Boolean = false) : ExposureStatus() {
        // FIXME use me
        fun daysSinceLastExposure(serverDate: Date): Int {
            return ((serverDate.time - lastExposureDate.time) / MILLIS_IN_A_DAY).toInt()
        }
    }

    @JsonClass(generateAdapter = true)
    data class Positive(@Transient private val foo: Int = 0) : ExposureStatus()
}
