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
 *  Created by mykhailo.nester on 5/5/21 6:59 PM
 */

package dgca.verifier.app.decoder

import com.upokecenter.cbor.CBORObject
import java.security.PublicKey
import java.security.Signature
import java.util.*
import kotlin.experimental.and

fun Signature.verify(verificationKey: PublicKey, dataToBeVerified: ByteArray, coseSignature: ByteArray): Boolean {
    initVerify(verificationKey)
    update(dataToBeVerified)

    return verify(coseSignature)
}

fun ByteArray.convertToDer(): ByteArray {
    val len = size / 2
    val r = copyOfRange(0, len)
    val s = copyOfRange(len, size)
    return encodeSignature(r, s)
}

private fun encodeSignature(r: ByteArray, s: ByteArray): ByteArray {
    val x = ArrayList<ByteArray>()
    x.add(r.unsignedInteger())
    x.add(s.unsignedInteger())
    return sequence(x)
}

private fun sequence(members: ArrayList<ByteArray>): ByteArray {
    val y = toBytes(members)
    val x = ArrayList<ByteArray>()
    x.add(byteArrayOf(0x30))
    x.add(computeLength(y.size))
    x.add(y)
    return toBytes(x)
}

private fun toBytes(x: ArrayList<ByteArray>): ByteArray {
    var l = 0
    l = x.stream().map { r: ByteArray -> r.size }.reduce(l) { i: Int, i1: Int -> Integer.sum(i, i1) }
    val b = ByteArray(l)
    l = 0
    for (r in x) {
        System.arraycopy(r, 0, b, l, r.size)
        l += r.size
    }
    return b
}

private fun computeLength(x: Int): ByteArray {
    return when {
        x <= 127 -> byteArrayOf(x.toByte())
        x < 256 -> byteArrayOf(0x81.toByte(), x.toByte())
        else -> throw Exception()
    }
}

private fun ByteArray.unsignedInteger(): ByteArray {
    var pad = 0
    var offset = 0
    while (offset < size && this[offset] == 0.toByte()) {
        offset++
    }
    if (offset == size) {
        return byteArrayOf(0x02, 0x01, 0x00)
    }
    if ((this[offset] and 0x80.toByte()) != 0.toByte()) {
        pad++
    }

    val length = size - offset
    val der = ByteArray(2 + length + pad)
    der[0] = 0x02
    der[1] = (length + pad).toByte()
    System.arraycopy(this, offset, der, 2 + pad, length)

    return der
}

fun ByteArray.getValidationDataFromCOSE(): ByteArray {
    val messageObject = CBORObject.DecodeFromBytes(this)
    val protectedHeader = messageObject[0].GetByteString()
    val content = messageObject[2].GetByteString()

    return CBORObject.NewArray().apply {
        Add("Signature1")
        Add(protectedHeader)
        Add(ByteArray(0))
        Add(content)
    }.EncodeToBytes()
}