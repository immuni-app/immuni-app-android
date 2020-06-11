/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.attestation

import android.content.Context
import android.util.Base64
import com.google.android.gms.safetynet.SafetyNet
import it.ministerodellasalute.immuni.extensions.utils.defaultMoshi
import it.ministerodellasalute.immuni.extensions.utils.fromJson
import it.ministerodellasalute.immuni.extensions.utils.log
import kotlinx.coroutines.tasks.await

class SafetyNetAttestationClient(
    private val context: Context,
    private val parameters: AttestationParameters
) : AttestationClient {
    data class AttestationParameters(
        internal val apiKey: String,
        internal val apkPackageName: String,
        internal val requiresBasicIntegrity: Boolean,
        internal val requiresCtsProfile: Boolean,
        internal val requiresHardwareAttestation: Boolean
    )

    override suspend fun attest(nonce: String): AttestationClient.Result {
        try {
            val nonceByteArray = Base64.decode(nonce, Base64.DEFAULT)
            val result =
                SafetyNet.getClient(context).attest(nonceByteArray, parameters.apiKey).await()
            val payload = String(Base64.decode(result.jwsResult.split(".")[1], Base64.DEFAULT))
            val jsonResult = defaultMoshi.fromJson<Map<String, Any>>(payload)!!
            if (nonce != jsonResult["nonce"]) {
                log("Attestation failed: non matching nonces")
                return AttestationClient.Result.Invalid
            }

            if (parameters.apkPackageName != jsonResult["apkPackageName"]) {
                log("Attestation failed: non matching apkPackageName")
                return AttestationClient.Result.Invalid
            }

            if (parameters.requiresBasicIntegrity && jsonResult["basicIntegrity"] != true) {
                log("Attestation failed: requiresBasicIntegrity")
                return AttestationClient.Result.Invalid
            }

            if (parameters.requiresCtsProfile && jsonResult["ctsProfileMatch"] != true) {
                log("Attestation failed: requiresCtsProfile")
                return AttestationClient.Result.Invalid
            }

            if (parameters.requiresHardwareAttestation) {
                val evaluationType = jsonResult["evaluationType"] as? String ?: return {
                    log("Attestation failed: evaluationType not present")
                    AttestationClient.Result.Invalid
                }()

                if (!evaluationType.contains("HARDWARE_BACKED")) {
                    log("Attestation failed: evaluationType does not contain HARDWARE_BACKED")
                    return AttestationClient.Result.Invalid
                }
            }

            return AttestationClient.Result.Success(result.jwsResult)
        } catch (e: Exception) {
            return AttestationClient.Result.Failure(e)
        }
    }
}
