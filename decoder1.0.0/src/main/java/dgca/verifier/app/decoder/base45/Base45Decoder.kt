/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by Mykhailo Nester on 4/23/21 9:50 AM
 */

package dgca.verifier.app.decoder.base45

import java.math.BigInteger

/**
 *  The Base45 Data Decoding
 *
 *  https://datatracker.ietf.org/doc/draft-faltstrom-base45/?include_text=1
 */
@ExperimentalUnsignedTypes
class Base45Decoder {

    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
    private val int45 = BigInteger.valueOf(45)

    fun decode(input: String) =
        input.chunked(3).map(this::decodeThreeCharsPadded)
            .flatten().map { it.toByte() }.toByteArray()

    private fun decodeThreeCharsPadded(input: String): List<UByte> {
        val result = decodeThreeChars(input).toMutableList()
        when (input.length) {
            3 -> while (result.size < 2) result += 0U
        }
        return result.reversed()
    }

    private fun decodeThreeChars(list: String) =
        generateSequenceByDivRem(fromThreeCharValue(list))
            .map { it.toUByte() }.toList()

    private fun fromThreeCharValue(list: String): Long {
        return list.foldIndexed(0L, { index, acc: Long, element ->
            if (!alphabet.contains(element)) throw IllegalArgumentException()
            pow(int45, index) * alphabet.indexOf(element) + acc
        })
    }

    private fun generateSequenceByDivRem(seed: Long) =
        generateSequence(seed) { if (it >= 256) it.div(256) else null }
            .map { it.rem(256).toInt() }

    private fun pow(base: BigInteger, exp: Int) = base.pow(exp).toLong()
}
