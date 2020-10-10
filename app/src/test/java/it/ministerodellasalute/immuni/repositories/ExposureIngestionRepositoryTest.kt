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

package it.ministerodellasalute.immuni.repositories

import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.api.services.defaultSettings
import it.ministerodellasalute.immuni.config.ExposureIngestionNetworkConfiguration
import it.ministerodellasalute.immuni.immuniMoshi
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureIngestionRepository
import java.util.*
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ExposureIngestionRepositoryTest {
    @Test
    fun `all exposure ingestion endpoints have the same size`() = runBlocking {
        val dummyUploadSize = sizeForRequest {
            it.validateOtp(
                isDummyData = 1,
                authorization = ExposureIngestionRepository.authorization("DUMMY")
            )
        }
        val validateOtpSize = sizeForRequest {
            it.validateOtp(
                isDummyData = 0,
                authorization = ExposureIngestionRepository.authorization("DUMMY")
            )
        }
        val emptyUploadTeksSize = sizeForRequest {
            it.uploadTeks(
                isDummyData = 0,
                authorization = ExposureIngestionRepository.authorization("DUMMY"),
                systemTime = Date().time.div(1000).toInt(),
                body = ExposureIngestionService.UploadTeksRequest(
                    teks = listOf(),
                    province = ExposureIngestionService.Province.agrigento,
                    exposureSummaries = listOf(),
                    countries = listOf()
                )
            )
        }
        val uploadTekSizeWithData = sizeForRequest {
            it.uploadTeks(
                isDummyData = 0,
                authorization = ExposureIngestionRepository.authorization("DUMMY"),
                systemTime = Date().time.div(1000).toInt(),
                body = ExposureIngestionService.UploadTeksRequest(
                    teks = (1..14).map {
                        ExposureIngestionService.TemporaryExposureKey(
                            keyData = "0".repeat(16),
                            rollingStartIntervalNumber = (Date().time / (1000 * 600)).toInt(),
                            rollingPeriod = 144
                        )
                    },
                    province = ExposureIngestionService.Province.agrigento,
                    exposureSummaries = (0..5).map {
                        ExposureIngestionService.ExposureSummary(
                            date = "2020-01-$it",
                            matchedKeyCount = it * 3,
                            daysSinceLastExposure = it * 5,
                            attenuationDurations = listOf(it * 7 % 30, it * 11 % 30, it * 13 % 30),
                            maximumRiskScore = (it * 17 % 8) + 1,
                            exposureInfo = (0..10).map {
                                ExposureIngestionService.ExposureInformation(
                                    date = "2020-01-$it",
                                    duration = it * 17 % 30,
                                    attenuationValue = 100,
                                    attenuationDurations = listOf(
                                        it * 7 % 30,
                                        it * 11 % 30,
                                        it * 13 % 30
                                    ),
                                    transmissionRiskLevel = (it * 19 % 8) + 1,
                                    totalRiskScore = it * 31 * 10 % 4096
                                )
                            }
                        )
                    },
                    countries = listOf("DK", "PL", "NL")
                )
            )
        }
        assertEquals(dummyUploadSize, emptyUploadTeksSize)
        assertEquals(dummyUploadSize, validateOtpSize)
        assertEquals(dummyUploadSize, uploadTekSizeWithData)
    }

    data class Info(
        val size: Long,
        val contentLengthDigitLength: Int
    )

    private suspend fun sizeForRequest(block: suspend (ExposureIngestionService) -> Unit): Info {
        val sizeCompleter = CompletableDeferred<Info>()
        val client = OkHttpClient.Builder().apply {
            addInterceptor(ExposureIngestionNetworkConfiguration.Interceptor { defaultSettings.teksPacketSize })
            addInterceptor { chain ->
                val request = chain.request()
                val contentLength = request.body!!.contentLength()
                val requestSize = request.url.encodedPath.length +
                    request.method.length +
                    request.headers.byteCount() +
                    contentLength

                sizeCompleter.complete(Info(requestSize, "$contentLength".length))

                Response.Builder()
                    .code(418) // Whatever code
                    .protocol(Protocol.HTTP_1_1)
                    .message("Dummy response")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .request(chain.request())
                    .build()
            }
        }.build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://upload.immuni.gov.it")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(immuniMoshi))
            .build()
        val service = retrofit.create(ExposureIngestionService::class.java)

        try {
            block(service)
        } catch (e: Exception) {
            // this is expected
        }
        return sizeCompleter.await()
    }
}
