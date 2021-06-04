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
 *  Created by mykhailo.nester on 4/24/21 3:06 PM
 */

package dgca.verifier.app.decoder.cose

import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.ECDSA_256
import dgca.verifier.app.decoder.RSA_PSS_256
import dgca.verifier.app.decoder.convertToDer
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.verify
import java.security.Signature
import java.security.cert.Certificate

class VerificationCryptoService : CryptoService {

    override fun validate(cose: ByteArray, certificate: Certificate, verificationResult: VerificationResult) {
        val verificationKey = certificate.publicKey
        verificationResult.coseVerified = try {
            val messageObject = CBORObject.DecodeFromBytes(cose)
            var coseSignature = messageObject.get(3).GetByteString()
            val protectedHeader = messageObject[0].GetByteString()
            val content = messageObject[2].GetByteString()
            val dataToBeVerified = getValidationData(protectedHeader, content)

            // get algorithm from header and verify signature
            when (CBORObject.DecodeFromBytes(protectedHeader).get(1).AsInt32Value()) {
                ECDSA_256 -> {
                    coseSignature = coseSignature.convertToDer()
                    Signature.getInstance(Algo.ALGO_ECDSA256.value).verify(
                        verificationKey,
                        dataToBeVerified,
                        coseSignature
                    )
                }
                RSA_PSS_256 ->
                    Signature.getInstance(Algo.ALGO_RSA256_PSS.value).verify(
                        verificationKey,
                        dataToBeVerified,
                        coseSignature
                    )
                else -> false
            }
        } catch (ex: Exception) {
            false
        }
    }

    private fun getValidationData(protected: ByteArray, content: ByteArray): ByteArray {
        return CBORObject.NewArray().apply {
            Add("Signature1")
            Add(protected)
            Add(ByteArray(0))
            Add(content)
        }.EncodeToBytes()
    }

    enum class Algo(val value: String) {
        ALGO_ECDSA256("SHA256withECDSA"),
        ALGO_RSA256_PSS("SHA256withRSA/PSS")
    }
}