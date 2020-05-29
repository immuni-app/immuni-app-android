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

package it.ministerodellasalute.immuni.logic.upload

import java.security.SecureRandom

class OtpGenerator(private val secureRandom: SecureRandom) {

    fun nextOtpCode(): String {
        return buildString {
            var checkSum = 0
            repeat(OTP_CODE_LENGTH - 1) {
                val nextCharIdx = secureRandom.nextInt(ALPHABET.size)
                val nextChar = ALPHABET[nextCharIdx]
                checkSum += CHECKSUM_MAP[nextChar]!![it and 1]

                append(nextChar)
            }
            val checkDigit = ALPHABET[checkSum % ALPHABET.size]
            append(checkDigit)
        }
    }

    fun prettify(code: String, separator: String): String {
        return buildString {
            code.forEachIndexed { index, c ->
                if (index in listOf(3, 7)) {
                    append(separator)
                }
                append(c)
            }
        }
    }
}

// Numerical char values depending on their position:
// arrayOf(odd, even)
val CHECKSUM_MAP = linkedMapOf(
    'A' to arrayOf(1, 0),
    'E' to arrayOf(9, 4),
    'F' to arrayOf(13, 5),
    'H' to arrayOf(17, 7),
    'I' to arrayOf(19, 8),
    'J' to arrayOf(21, 9),
    'K' to arrayOf(2, 10),
    'L' to arrayOf(4, 11),
    'Q' to arrayOf(6, 16),
    'R' to arrayOf(8, 17),
    'S' to arrayOf(12, 18),
    'U' to arrayOf(16, 20),
    'W' to arrayOf(22, 22),
    'X' to arrayOf(25, 23),
    'Y' to arrayOf(24, 24),
    'Z' to arrayOf(23, 25),
    '1' to arrayOf(0, 1),
    '2' to arrayOf(5, 2),
    '3' to arrayOf(7, 3),
    '4' to arrayOf(9, 4),
    '5' to arrayOf(13, 5),
    '6' to arrayOf(15, 6),
    '7' to arrayOf(17, 7),
    '8' to arrayOf(19, 8),
    '9' to arrayOf(21, 9)
)
val ALPHABET = CHECKSUM_MAP.keys.toCharArray()
const val OTP_CODE_LENGTH = 10
