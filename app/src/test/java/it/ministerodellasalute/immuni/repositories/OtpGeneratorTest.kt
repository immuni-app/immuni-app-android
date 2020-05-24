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

package it.ministerodellasalute.immuni.repositories

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import it.ministerodellasalute.immuni.logic.upload.ALPHABET
import it.ministerodellasalute.immuni.logic.upload.OTP_CODE_LENGTH
import it.ministerodellasalute.immuni.logic.upload.OtpGenerator
import java.security.SecureRandom
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class OtpGeneratorTest {

    @MockK(relaxed = true)
    lateinit var secureRandom: SecureRandom

    private lateinit var generator: OtpGenerator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        generator = OtpGenerator(secureRandom)
    }

    @Test
    fun `generates codes with correct check digit`() {
        val inputs = mapOf(
            "9K2RAY8UU" to "9K2-RAY8-UUQ",
            "UQ776R9A7" to "UQ7-76R9-A7E",
            "Z6UI7HAFX" to "Z6U-I7HA-FXQ",
            "4K51Q1923" to "4K5-1Q19-235",
            "A6HHFALQK" to "A6H-HFAL-QK1",
            "7WRQXW27E" to "7WR-QXW2-7EK",
            "A23X28L7F" to "A23-X28L-7F5",
            "I4A7KRXJ4" to "I4A-7KRX-J43",
            "QL412K779" to "QL4-12K7-79W",
            "6IJ3E7A9Q" to "6IJ-3E7A-9QI"
        )
        inputs.forEach { (mockDigits, expectedCode) ->
            // Random number generator returns indexes for this code
            every {
                secureRandom.nextInt(ALPHABET.size)
            }.returnsMany(mockDigits.map { ALPHABET.indexOf(it) })

            val code = generator.prettify(generator.nextOtpCode(), "-")
            assertEquals(expectedCode, code, "Was expecting [$expectedCode], but generated [$code]")
        }
        verify(exactly = inputs.size * (OTP_CODE_LENGTH - 1)) { secureRandom.nextInt(any()) }
    }
}
