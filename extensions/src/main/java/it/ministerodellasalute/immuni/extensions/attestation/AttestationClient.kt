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

interface AttestationClient {
    sealed class Result {
        // use this class to signal that the attestation was done and (to the best of
        // client's knowledge) is also valid
        data class Success(val result: String) : Result()

        // use this class to signal that the attestation was done but the outcome is not valid
        object Invalid : Result()

        // use this class to signal a temporary error during the attestation process, and that
        // a new attempt may be done in the future
        data class Failure(val error: Exception) : Result()
    }

    suspend fun attest(nonce: String): Result
}
