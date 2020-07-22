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
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class ForceUpdateNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    private val notificationManager: AppNotificationManager by inject()
    private val workerManager: WorkerManager by inject()
    private val settingsManager: ConfigurationSettingsManager by inject()
    private val forceUpdateManager: ForceUpdateManager by inject()

    override suspend fun doWork(): Result {

        // DEBUG notification
        if (applicationContext.resources.getBoolean(R.bool.development_device)) {
            notificationManager.triggerDebugNotification("Force Update Worker.")
        }

        try {
            settingsManager.fetchSettingsAsync().await()
            if (!forceUpdateManager.isAppOutdated) {
                return Result.success()
            }

            notificationManager.triggerNotification(NotificationType.ForcedVersionUpdate)
            workerManager.scheduleForceUpdateNotificationWorker()

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
