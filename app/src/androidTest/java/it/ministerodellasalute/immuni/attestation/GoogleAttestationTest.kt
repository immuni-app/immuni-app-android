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

package it.ministerodellasalute.immuni.attestation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.ministerodellasalute.immuni.BuildConfig
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.extensions.attestation.SafetyNetAttestationClient
import it.ministerodellasalute.immuni.extensions.utils.base64EncodedSha256
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoogleAttestationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testAttestationSucceeds() = runBlocking {
        val client = SafetyNetAttestationClient(
            context = context,
            parameters = SafetyNetAttestationClient.AttestationParameters(
                apiKey = BuildConfig.SAFETY_NET_TEST_API_KEY,
                apkPackageName = context.packageName,
                requiresBasicIntegrity = true,
                requiresCtsProfile = true,
                requiresHardwareAttestation = true
            )
        )

        val response = client.attest("nonce".base64EncodedSha256())

        assertTrue(response is AttestationClient.Result.Success)
    }
}
