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
import androidx.annotation.VisibleForTesting
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.extensions.utils.exponential
import it.ministerodellasalute.immuni.extensions.utils.log
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import java.security.SecureRandom
import java.util.*
import kotlin.math.min
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.KoinComponent
import org.koin.core.inject

class DummyExposureIngestionWorker(
    appContext: Context,
    params: WorkerParameters
) :
    CoroutineWorker(appContext, params), KoinComponent {
    private val workerManager: WorkerManager by inject()
    private val appLifecycleObserver: AppLifecycleObserver by inject()
    private val exposureManager: ExposureManager by inject()
    private val settingsManager: ConfigurationSettingsManager by inject()
    private val notificationManager: AppNotificationManager by inject()
    private val random = SecureRandom()

    override suspend fun doWork(): Result {

        // DEBUG notification
        if (applicationContext.resources.getBoolean(R.bool.development_device)) {
            notificationManager.triggerDebugNotification("Dummy Injestion Worker.")
        }

        val settings = settingsManager.settings.value
        val impl = Impl(
            configuration = Configuration(
                teksAverageRequestWaitingTime = settings.dummyTeksAverageRequestWaitingTime,
                teksRequestProbabilities = settings.dummyTeksRequestProbabilities
            ),
            workerManager = workerManager,
            appLifecycleObserver = appLifecycleObserver,
            exposureManager = exposureManager,
            random = random
        )

        return impl.doWork()
    }

    class Impl(
        private val configuration: Configuration,
        private val workerManager: WorkerManager,
        private val appLifecycleObserver: AppLifecycleObserver,
        private val exposureManager: ExposureManager,
        private val random: Random = SecureRandom()
    ) {
        private var counter = 0
        suspend fun doWork(): Result {
            try {
                // keep the maximum execution time within the 10 minutes limit imposed by WorkManager
                withTimeout(9 * 60 * 1000L) {
                    // cancel and reschedule if the app goes to foreground while executing this work
                    if (appLifecycleObserver.isInForeground.value) {
                        throw Exception("App is in foreground")
                    }
                    val isInForegroundJob = appLifecycleObserver.isInForeground
                        .filter { it }
                        .onEach { cancel() }
                        .launchIn(this)

                    while (shouldPerformNextUpload()) {
                        counter += 1
                        performDummyUpload()
                        waitForNextUpload()
                    }
                    isInForegroundJob.cancel()
                }
            } catch (e: Exception) {
                log("dummy exposure ingestion worker failed: $e")
            }

            workerManager.scheduleNextDummyExposureIngestionWorker()
            return Result.success()
        }

        private fun shouldPerformNextUpload(): Boolean {
            if (counter == 0) {
                return true
            }
            val probabilities = configuration.teksRequestProbabilities
            val probability = probabilities[min(counter - 1, probabilities.count() - 1)]
            return random.nextDouble() < probability
        }

        @VisibleForTesting
        suspend fun performDummyUpload(): Boolean {
            return exposureManager.dummyUpload()
        }

        private suspend fun waitForNextUpload() {
            val timeToWait =
                random.exponential(configuration.teksAverageRequestWaitingTime.toLong() * 1000)
            delay(timeToWait)
        }
    }

    data class Configuration(
        val teksAverageRequestWaitingTime: Int,
        val teksRequestProbabilities: List<Double>
    )
}
