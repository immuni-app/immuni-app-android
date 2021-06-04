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
 *  Created by mykhailo.nester on 4/30/21 4:38 PM
 */

package dgca.verifier.app.decoder

import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.model.KeyPairData
import java.io.ByteArrayInputStream
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

const val ECDSA_256 = -7
const val RSA_PSS_256 = -37

fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)

fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

fun String.hexToByteArray(): ByteArray = chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()

fun String.fromBase64(): ByteArray = Base64.getDecoder().decode(this)

fun String.base64ToX509Certificate(): X509Certificate? {
    val decoded = android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
    val inputStream = ByteArrayInputStream(decoded)

    return CertificateFactory.getInstance("X.509").generateCertificate(inputStream) as? X509Certificate
}

fun ByteArray.toHash(): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(this)
        .toBase64()
}

fun ByteArray.generateKeyPair(): KeyPairData? {
    val messageObject = CBORObject.DecodeFromBytes(this)
    val protectedHeader = messageObject[0].GetByteString()

    // get algorithm from header
    when (CBORObject.DecodeFromBytes(protectedHeader).get(1).AsInt32Value()) {
        ECDSA_256 -> {
            val keyPairGen = KeyPairGenerator.getInstance("EC")
            keyPairGen.initialize(256)
            return KeyPairData("SHA256withECDSA", keyPairGen.generateKeyPair())
        }
        RSA_PSS_256 -> {
            val keyPairGen = KeyPairGenerator.getInstance("RSA")
            keyPairGen.initialize(2048)
            return KeyPairData("SHA256WithRSA", keyPairGen.generateKeyPair())
        }
    }

    return null
}