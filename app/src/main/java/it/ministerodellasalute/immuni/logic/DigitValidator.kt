package it.ministerodellasalute.immuni.logic

import it.ministerodellasalute.immuni.logic.exposure.models.GreenPassValidationResult
import it.ministerodellasalute.immuni.logic.upload.CHECK_DIGIT_MAP
import it.ministerodellasalute.immuni.logic.upload.EVEN_MAP
import it.ministerodellasalute.immuni.logic.upload.ODD_MAP

class DigitValidator {

    fun validaCheckDigitCUN(token: String): GreenPassValidationResult {
        var checkSum = 0
        repeat(CUN_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % 25]

        return if (checkDigit == token[CUN_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitOTP(token: String): GreenPassValidationResult {
        var checkSum = 0
        repeat(OTP_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % 25]

        return if (checkDigit == token[OTP_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitNRFE(token: String): GreenPassValidationResult {
        var checkSum = 0
        repeat(CUN_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % 25]

        return if (checkDigit == token[CUN_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }

    fun validaCheckDigitNUCG(token: String): GreenPassValidationResult {
        var checkSum = 0
        repeat(CUN_CODE_LENGTH - 1) {
            val char = token[it]
            checkSum += (if (it.isEven) ODD_MAP else EVEN_MAP).getValue(char)
        }

        val checkDigit = CHECK_DIGIT_MAP[checkSum % 25]

        return if (checkDigit == token[CUN_CODE_LENGTH - 1]) {
            GreenPassValidationResult.Valid(true)
        } else {
            GreenPassValidationResult.TokenWrong
        }
    }
}

private inline val Int.isEven get() = (this and 1) == 0

const val CUN_CODE_LENGTH = 10
const val OTP_CODE_LENGTH = 10
