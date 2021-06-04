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
 *  Created by Mykhailo Nester on 4/23/21 9:51 AM
 */

package dgca.verifier.app.decoder.cose

import COSE.HeaderKeys
import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.model.CoseData
import dgca.verifier.app.decoder.model.VerificationResult

/**
 * Decodes input according to COSE specification (RFC8152)
 */
class DefaultCoseService : CoseService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): CoseData? {
        return try {
            val messageObject = CBORObject.DecodeFromBytes(input)
            val content = messageObject[2].GetByteString()
            val protectedHeader = messageObject[0].GetByteString()
            val unprotectedHeader = messageObject[1]
            val kid = getKid(protectedHeader, unprotectedHeader)
            val kidByteString = kid?.GetByteString()
            CoseData(content, kidByteString)
        } catch (e: Throwable) {
            null
        }
    }

    private fun getKid(protectedHeader: ByteArray, unprotectedHeader: CBORObject): CBORObject? {
        val key = HeaderKeys.KID.AsCBOR()
        return if (protectedHeader.isNotEmpty()) {
            try {
                val kid = CBORObject.DecodeFromBytes(protectedHeader).get(key)
                kid ?: unprotectedHeader.get(key)
            } catch (ex: Exception) {
                unprotectedHeader.get(key)
            }
        } else {
            unprotectedHeader.get(key)
        }
    }
}
