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

package it.ministerodellasalute.immuni.util

import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult

class DigitValidator {

    fun validaCheckDigitCUN(token: String): GreenPassValidationResult {
        if (token.length != CUN_CODE_LENGTH) {
            return GreenPassValidationResult.TokenLengthWrong
        }
        var checkSum = 0
        repeat(CUN_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP_CUN[checkSum % ALPHABET_CUN.size]

        return if (checkDigit == token[CUN_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitAuthcode(token: String): GreenPassValidationResult {
        if (token.length != OTP_CODE_LENGTH) {
            return GreenPassValidationResult.TokenLengthWrong
        }
        var checkSum = 0
        repeat(OTP_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % ALPHABET.size]

        return if (checkDigit == token[OTP_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitNRFE(token: String): GreenPassValidationResult {
        return if (token.length == NRFE_CODE_LENGTH && token.substring(0, 2) == NRFE_START_WITH) {
            GreenPassValidationResult.Valid(true)
        } else if (token.length != NRFE_CODE_LENGTH) {
            GreenPassValidationResult.TokenLengthWrong
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitNUCG(token: String): GreenPassValidationResult {
        if (token.length != NUCG_CODE_LENGTH) {
            return GreenPassValidationResult.TokenLengthWrong
        }
        var checkSum = 0
        repeat(NUCG_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % ALPHABET.size]

        return if (checkDigit == token[NUCG_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitCUEV(token: String): GreenPassValidationResult {
        if (token.length != CUEV_CODE_LENGTH) {
            return GreenPassValidationResult.TokenLengthWrong
        }
        var checkSum = 0
        repeat(CUEV_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % ALPHABET.size]

        return if (checkDigit == token[CUEV_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }
}

private inline val Int.isEven get() = (this and 1) == 0

const val CUN_CODE_LENGTH = 10
const val NRFE_CODE_LENGTH = 17
const val NUCG_CODE_LENGTH = 10
const val CUEV_CODE_LENGTH = 10
const val OTP_CODE_LENGTH = 12
const val NRFE_START_WITH = "99"

val ODD_MAP = mapOf(
    '0' to 1,
    '1' to 0,
    '2' to 5,
    '3' to 7,
    '4' to 9,
    '5' to 13,
    '6' to 15,
    '7' to 17,
    '8' to 19,
    '9' to 21,
    'A' to 1,
    'B' to 0,
    'C' to 5,
    'D' to 7,
    'E' to 9,
    'F' to 13,
    'G' to 15,
    'H' to 17,
    'I' to 19,
    'J' to 21,
    'K' to 2,
    'L' to 4,
    'M' to 18,
    'N' to 20,
    'O' to 11,
    'P' to 3,
    'Q' to 6,
    'R' to 8,
    'S' to 12,
    'T' to 14,
    'U' to 16,
    'V' to 10,
    'W' to 22,
    'X' to 25,
    'Y' to 24,
    'Z' to 23
)
val EVEN_MAP = mapOf(
    '0' to 0,
    '1' to 1,
    '2' to 2,
    '3' to 3,
    '4' to 4,
    '5' to 5,
    '6' to 6,
    '7' to 7,
    '8' to 8,
    '9' to 9,
    'A' to 0,
    'B' to 1,
    'C' to 2,
    'D' to 3,
    'E' to 4,
    'F' to 5,
    'G' to 6,
    'H' to 7,
    'I' to 8,
    'J' to 9,
    'K' to 10,
    'L' to 11,
    'M' to 12,
    'N' to 13,
    'O' to 14,
    'P' to 15,
    'Q' to 16,
    'R' to 17,
    'S' to 18,
    'T' to 19,
    'U' to 20,
    'V' to 21,
    'W' to 22,
    'X' to 23,
    'Y' to 24,
    'Z' to 25
)
val ALPHABET_CUN = arrayOf(
    'A',
    'E',
    'F',
    'H',
    'I',
    'J',
    'K',
    'L',
    'Q',
    'R',
    'S',
    'U',
    'W',
    'X',
    'Y',
    'Z',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9'
)
val ALPHABET = arrayOf(
    'A',
    'B',
    'C',
    'D',
    'E',
    'F',
    'H',
    'I',
    'J',
    'K',
    'L',
    'M',
    'N',
    'P',
    'Q',
    'R',
    'S',
    'T',
    'U',
    'V',
    'W',
    'X',
    'Y',
    'Z',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9'
)

val CHECK_DIGIT_MAP_CUN = ALPHABET_CUN.asSequence().mapIndexed { index, c -> index to c }.toMap()
val CHECK_DIGIT_MAP =
    ALPHABET.asSequence().mapIndexed { index, c -> index to c }.toMap()
