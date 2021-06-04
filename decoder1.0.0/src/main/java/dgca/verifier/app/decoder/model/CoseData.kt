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
 *  Created by mykhailo.nester on 4/24/21 3:42 PM
 */

package dgca.verifier.app.decoder.model

data class CoseData(
    val cbor: ByteArray,
    val kid: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoseData

        if (!cbor.contentEquals(other.cbor)) return false
        if (kid != null) {
            if (other.kid == null) return false
            if (!kid.contentEquals(other.kid)) return false
        } else if (other.kid != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cbor.contentHashCode()
        result = 31 * result + (kid?.contentHashCode() ?: 0)
        return result
    }
}