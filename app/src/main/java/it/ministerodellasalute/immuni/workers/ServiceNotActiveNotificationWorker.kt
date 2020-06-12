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
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import it.ministerodellasalute.immuni.workers.models.ServiceNotActiveNotificationWorkerStatus
import it.ministerodellasalute.immuni.workers.repositories.ServiceNotActiveNotificationWorkerRepository
import java.util.*
import kotlinx.coroutines.delay
import org.koin.core.KoinComponent
import org.koin.core.inject

class ServiceNotActiveNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    private val repository: ServiceNotActiveNotificationWorkerRepository by inject()
    private val settingsManager: ConfigurationSettingsManager by inject()
    private val exposureManager: ExposureManager by inject()
    private val notificationManager: AppNotificationManager by inject()
    private val workerManager: WorkerManager by inject()

    override suspend fun doWork(): Result {

        // DEBUG notification
        if (applicationContext.resources.getBoolean(R.bool.development_device)) {
            notificationManager.triggerDebugNotification("Service Not Active Worker.")
        }

        Impl(
            Date(),
            settingsManager.settings.value.serviceNotActiveNotificationPeriod,
            exposureManager,
            repository,
            notificationManager
        ).doWork()

        workerManager.scheduleServiceNotActiveNotificationWorker(ExistingWorkPolicy.REPLACE)
        return Result.success()
    }

    class Impl(
        private val currentDate: Date,
        private val serviceNotActiveNotificationPeriod: Int,
        private val exposureManager: ExposureManager,
        private val repository: ServiceNotActiveNotificationWorkerRepository,
        private val notificationManager: AppNotificationManager
    ) {
        suspend fun doWork() {
            val serviceIsActive = exposureManager.updateAndGetServiceIsActive()

            // give time to the listeners to propagate the updated status
            delay(5000)

            if (exposureManager.isBroadcastingActive.value == true) {
                if (repository.status !is ServiceNotActiveNotificationWorkerStatus.Working) {
                    repository.status = ServiceNotActiveNotificationWorkerStatus.Working()
                }

                return
            }
            val pastStatus = repository.status ?: return

            when (pastStatus) {
                is ServiceNotActiveNotificationWorkerStatus.Working -> {
                    // if the past status was `Working`, we trigger a notification only if google play services has
                    // not already sent one (i.e. the service itself has been disabled)
                    if (!serviceIsActive) {
                        notificationManager.triggerNotification(NotificationType.ServiceNotActive)
                    }

                    repository.status =
                        ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
                }
                is ServiceNotActiveNotificationWorkerStatus.NotWorking -> {
                    val secondsSinceLastNotification =
                        (currentDate.time - pastStatus.lastNotificationTime.time) / 1000

                    // time is skewed, reset everything
                    if (secondsSinceLastNotification < 0) {
                        repository.status =
                            ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
                    }
                    if (secondsSinceLastNotification >= serviceNotActiveNotificationPeriod) {
                        notificationManager.triggerNotification(NotificationType.ServiceNotActive)
                        repository.status =
                            ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
                    }
                }
            }
        }
    }
}
