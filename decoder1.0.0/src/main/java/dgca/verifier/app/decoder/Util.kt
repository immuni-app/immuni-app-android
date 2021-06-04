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
 *  Created by mykhailo.nester on 5/11/21 11:12 PM
 */

package dgca.verifier.app.decoder

import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.Signature
import java.util.Base64

fun generateClaimSignature(
    tanHash: String,
    certHash: String,
    publicKey: String,
    privateKey: PrivateKey, sigAlg: String
): String {
    val sigValue = StringBuilder()
    sigValue.append(tanHash)
        .append(certHash)
        .append(publicKey)
    val signature = Signature.getInstance(sigAlg)
    signature.initSign(privateKey)
    signature.update(sigValue.toString().toByteArray(StandardCharsets.UTF_8))
    val sigData = signature.sign()

    return Base64.getEncoder().encodeToString(sigData)
}