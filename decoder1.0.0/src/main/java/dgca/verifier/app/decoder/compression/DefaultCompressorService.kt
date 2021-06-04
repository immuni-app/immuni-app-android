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

package dgca.verifier.app.decoder.compression

import dgca.verifier.app.decoder.model.VerificationResult
import java.util.zip.InflaterInputStream

class DefaultCompressorService : CompressorService {

    /**
     * *Optionally* decompresses input with ZLIB = inflating.
     *
     * If the [input] does not start with ZLIB magic numbers (0x78), no decompression happens
     */
    override fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray {
        verificationResult.zlibDecoded = false
        if (input.size >= 2 && input[0] == 0x78.toByte()) { // ZLIB magic headers
            if (input[1] == 0x01.toByte() || // Level 1
                input[1] == 0x5E.toByte() || // Level 2 - 5
                input[1] == 0x9C.toByte() || // Level 6
                input[1] == 0xDA.toByte()    // Level 7 - 9
            ) {
                return try {
                    InflaterInputStream(input.inputStream()).readBytes().also {
                        verificationResult.zlibDecoded = true
                    }
                } catch (e: Throwable) {
                    input
                }
            }
        }
        return input
    }
}