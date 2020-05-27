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
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import it.ministerodellasalute.immuni.logic.forceupdate.PlayServicesStatus
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class ServiceNotActiveNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    private val exposureManager: ExposureManager by inject()
    private val notificationManager: AppNotificationManager by inject()
    private val workerManager: WorkerManager by inject()

    override suspend fun doWork(): Result {
        return doWork(
            exposureManager.isBroadcastingActive.value ?: false,
            notificationManager,
            workerManager
        )
    }

    companion object {
        fun doWork(
            isServiceActive: Boolean,
            notificationManager: AppNotificationManager,
            workerManager: WorkerManager
        ): Result {
            if (!isServiceActive) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }

            

            return Result.success()
        }
    }
}
