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

package dgca.verifier.app.decoder.cwt

import com.upokecenter.cbor.CBORObject

/**
 * Adapted from [COSE.HeaderKeys] to use CWT specific ones (https://tools.ietf.org/html/rfc8392)
 */
@Suppress("ClassName")
sealed class CwtHeaderKeys(value: Int) {

    private val value: CBORObject = CBORObject.FromObject(value)

    fun asCBOR(): CBORObject {
        return value
    }

    object ISSUER : CwtHeaderKeys(1)
    object SUBJECT : CwtHeaderKeys(2)
    object AUDIENCE : CwtHeaderKeys(3)
    object EXPIRATION : CwtHeaderKeys(4)
    object NOT_BEFORE : CwtHeaderKeys(5)
    object ISSUED_AT : CwtHeaderKeys(6)
    object CWT_ID : CwtHeaderKeys(7)

    object HCERT : CwtHeaderKeys(-260)
}