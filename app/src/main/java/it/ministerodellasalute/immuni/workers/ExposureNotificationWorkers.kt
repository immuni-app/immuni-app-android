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
            val settingsResult = settingsManager.fetchSettingsAsync().await()
            if (settingsResult is FetchSettingsResult.Success) {
                // cleanup entities that are older than DAYS_OF_SELF_ISOLATION
                exposureReportingRepository.deleteOldSummaries(settingsResult.serverDate)
            } else {
                return Result.retry()
            }

            if (settingsManager.isAppOutdated) {
                return Result.retry()
            }

            chunksDir.apply {
                deleteRecursively()
                mkdir()
            }

            val indexResponse = immuniApiCall { api.index() }

            if (indexResponse !is NetworkResource.Success) {
                val error = indexResponse.error
                // 404 means that the list is empty, so the work is successful
                if (error is NetworkError.HttpError && error.httpCode == 404) {
                    return success()
                }

                return Result.retry()
            }

            val data = indexResponse.data ?: return Result.retry()
            val currentOldest =
                max(data.oldest, exposureReportingRepository.lastProcessedChunk(default = 0) + 1)

            val keyFiles = mutableListOf<File>()
            for (currentChunk in currentOldest..data.newest) {
                val chunkResponse = immuniApiCall { api.chunk(currentChunk) }
                if (chunkResponse !is NetworkResource.Success) {
                    return Result.retry()
                }
                val filePath =
                    listOf(chunksDirPath, "$currentChunk.zip").joinToString(File.separator)
                try {
                    chunkResponse.data?.byteStream()?.saveToFile(filePath) ?: return Result.retry()
                    keyFiles.add(File(filePath))
                } catch (e: Exception) {
                    return Result.retry()
                }
            }

            val token = "${UUID.randomUUID()}_${settingsResult.serverDate.time}"

            if (keyFiles.isNotEmpty()) {
                exposureManager.provideDiagnosisKeys(
                    keyFiles = keyFiles,
                    token = token
                )
                exposureReportingRepository.setLastProcessedChunk(data.newest)
            }

            return success()
        } catch (e: Exception) {
            return Result.retry()
        } finally {
            chunksDir.deleteRecursively()
        }
    }

    private fun success(): Result {
        workerManager.scheduleNextDiagnosisKeysRequest()
        return Result.success()
    }
}

private fun InputStream.saveToFile(file: String) = use { input ->
    File(file).outputStream().use { output ->
        input.copyTo(output)
    }
}
