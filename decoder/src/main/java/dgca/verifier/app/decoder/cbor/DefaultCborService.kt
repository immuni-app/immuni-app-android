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

package dgca.verifier.app.decoder.cbor

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.cwt.CwtHeaderKeys
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.VerificationResult
import java.time.Instant

/**
 * Decodes input as a CBOR structure
 */
class DefaultCborService : CborService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): GreenCertificate? {
        verificationResult.cborDecoded = false
        try {
            val map = CBORObject.DecodeFromBytes(input)

            val issuedAt = Instant.ofEpochSecond(map[CwtHeaderKeys.ISSUED_AT.asCBOR()].AsInt64())
            verificationResult.isIssuedTimeCorrect = issuedAt.isBefore(Instant.now())

            val expirationTime = Instant.ofEpochSecond(map[CwtHeaderKeys.EXPIRATION.asCBOR()].AsInt64())
            verificationResult.isNotExpired = expirationTime.isAfter(Instant.now())

            val hcert = map[CwtHeaderKeys.HCERT.asCBOR()]
            val hcertv1 = hcert[CBORObject.FromObject(1)].EncodeToBytes()

            return CBORMapper()
                .readValue(hcertv1, GreenCertificate::class.java)
                .also { verificationResult.cborDecoded = true }
        } catch (e: Throwable) {
            return null
        }
    }
}
