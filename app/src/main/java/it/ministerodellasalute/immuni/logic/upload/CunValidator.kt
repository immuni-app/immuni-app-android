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

import it.ministerodellasalute.immuni.logic.exposure.models.CunToken
import it.ministerodellasalute.immuni.logic.exposure.models.CunValidationResult

class CunValidator {

    fun validaCheckDigitCUN(cun: String): CunValidationResult {
        var checkSum = 0
        repeat(CUN_CODE_LENGTH - 1) {
            val char = cun[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % 25]

        return if (checkDigit == cun[CUN_CODE_LENGTH - 1]) {
            CunValidationResult.Success(CunToken(cun, null))
        } else {
            CunValidationResult.CunWrong
        }
    }
}

private inline val Int.isEven get() = (this and 1) == 0

const val CUN_CODE_LENGTH = 10
