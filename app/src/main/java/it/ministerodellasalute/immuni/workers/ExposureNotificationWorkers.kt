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

package it.ministerodellasalute.immuni.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import it.ministerodellasalute.immuni.api.immuniApiCall
import it.ministerodellasalute.immuni.api.services.ExposureReportingService
import it.ministerodellasalute.immuni.logic.exposure.ExposureAnalyticsManager
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.models.FetchSettingsResult
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import it.ministerodellasalute.immuni.network.api.NetworkError
import it.ministerodellasalute.immuni.network.api.NetworkResource
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.math.max
import kotlinx.coroutines.withTimeout
import org.koin.core.KoinComponent
import org.koin.core.inject

class StateUpdatedWorker(
    appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    companion object {
        const val TOKEN_KEY = "token"
    }

    private val exposureManager: ExposureManager by inject()

    override suspend fun doWork(): Result {
        val token = params.inputData.getString(TOKEN_KEY)!!
        val serverTimestamp = token.split("_")[1].toLong()
        val serverDate = Date(serverTimestamp)

        exposureManager.startProcessingKeys(token, serverDate)
        return Result.success()
    }
}

class RequestDiagnosisKeysWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val exposureReportingRepository: ExposureReportingRepository by inject()
    private val exposureManager: ExposureManager by inject()
    private val analyticsManager: ExposureAnalyticsManager by inject()
    private val workerManager: WorkerManager by inject()
    private val api: ExposureReportingService by inject()
    private val settingsManager: ConfigurationSettingsManager by inject()

    override suspend fun doWork(): Result {
        val chunksDirPath = listOf(
            applicationContext.filesDir,
            "chunks"
        ).joinToString(File.separator)
        val chunksDir = File(chunksDirPath)

        try {
            // keep the maximum execution time within the 10 minutes limit imposed by WorkManager
            return withTimeout(9 * 60 * 1000) {
                val settingsResult = settingsManager.fetchSettingsAsync().await()
                if (settingsResult is FetchSettingsResult.Success) {
                    // cleanup entities that are older than DAYS_OF_SELF_ISOLATION
                    exposureReportingRepository.deleteOldSummaries(settingsResult.serverDate)
                } else {
                    return@withTimeout Result.retry()
                }

                if (settingsManager.isAppOutdated) {
                    return@withTimeout Result.retry()
                }

                val serverDate = settingsResult.serverDate

                analyticsManager.setup(serverDate)

                chunksDir.apply {
                    deleteRecursively()
                    mkdir()
                }

                val indexResponse = immuniApiCall { api.index() }

                if (indexResponse !is NetworkResource.Success) {
                    val error = indexResponse.error
                    // 404 means that the list is empty, so the work is successful
                    if (error is NetworkError.HttpError && error.httpCode == 404) {
                        return@withTimeout success(serverDate)
                    }

                    return@withTimeout Result.retry()
                }

                val data = indexResponse.data ?: return@withTimeout Result.retry()
                val lastProcessedChunkSuccessor = exposureReportingRepository.lastProcessedChunk(default = 0) + 1
                val currentOldest = max(data.oldest, lastProcessedChunkSuccessor)
                val chunkRange = (currentOldest..data.newest).toList().takeLast(20)

                for (currentChunk in chunkRange) {
                    val chunkResponse = immuniApiCall { api.chunk(currentChunk) }
                    if (chunkResponse !is NetworkResource.Success) {
                        return@withTimeout Result.retry()
                    }
                    val filePath =
                        listOf(chunksDirPath, "$currentChunk.zip").joinToString(File.separator)
                    try {
                        chunkResponse.data?.byteStream()?.saveToFile(filePath)
                            ?: return@withTimeout Result.retry()
                        val token = "${UUID.randomUUID()}_${serverDate.time}"
                        exposureManager.provideDiagnosisKeys(
                            keyFiles = listOf(File(filePath)),
                            token = token
                        )
                        exposureReportingRepository.setLastProcessedChunk(data.newest)
                    } catch (e: Exception) {
                        return@withTimeout Result.retry()
                    }
                }
                return@withTimeout success(serverDate)
            }
        } catch (e: Exception) {
            return Result.retry()
        } finally {
            chunksDir.deleteRecursively()
        }
    }

    private fun success(serverDate: Date): Result {
        workerManager.scheduleExposureAnalyticsWorker(serverDate)
        workerManager.scheduleNextDiagnosisKeysRequest()
        return Result.success()
    }
}

private fun InputStream.saveToFile(file: String) = use { input ->
    File(file).outputStream().use { output ->
        input.copyTo(output)
    }
}
