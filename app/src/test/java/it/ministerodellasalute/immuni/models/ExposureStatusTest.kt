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

package it.ministerodellasalute.immuni.models

import it.ministerodellasalute.immuni.extensions.utils.fromJson
import it.ministerodellasalute.immuni.extensions.utils.moshi
import it.ministerodellasalute.immuni.extensions.utils.toJson
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import java.util.*
import kotlin.test.assertEquals
import org.junit.Test

class ExposureStatusTest {

    @Test
    fun `test ExposureStatus serialization`() {
        val moshi = moshi(extraFactories = listOf(
            ExposureStatus.moshiAdapter
        ))
        val status = ExposureStatus.Exposed(lastExposureDate = Date())

        val serialized = moshi.toJson<ExposureStatus>(status)
        val deserialzed = moshi.fromJson<ExposureStatus>(serialized)
        assertEquals(status, deserialzed)
    }
}
