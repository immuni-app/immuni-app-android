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

package it.ministerodellasalute.immuni.logic.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import it.ministerodellasalute.immuni.extensions.utils.exponential
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.workers.ForceUpdateNotificationWorker
import it.ministerodellasalute.immuni.workers.OnboardingNotCompletedWorker
import it.ministerodellasalute.immuni.workers.RequestDiagnosisKeysWorker
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.KoinComponent

class WorkerManager(
    context: Context,
    private val settingsManager: ConfigurationSettingsManager,
    private val notificationManager: AppNotificationManager,
    private val workManager: WorkManager = WorkManager.getInstance(context)
) : KoinComponent {
    private val settings get() = settingsManager.settings.value
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    init {
        settingsManager.settings.onEach {
            if (settingsManager.isAppOutdated) {
                scheduleForceUpdateNotificationWorker(withDelay = false)
            } else {
                notificationManager.removeNotification(NotificationType.ForcedVersionUpdate)
            }
        }.launchIn(scope)
    }

    fun scheduleOnboardingNotCompletedWorker(policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        workManager.enqueueUniqueWork(
            "OnboardingNotCompletedWorker",
            policy,
            OneTimeWorkRequest.Builder(OnboardingNotCompletedWorker::class.java)
                .setInitialDelay(
                    settings.onboardingNotCompletedNotificationPeriod.toLong(),
                    TimeUnit.SECONDS
                )
                .build()
        )
    }

    fun scheduleForceUpdateNotificationWorker(withDelay: Boolean) {
        val delay = settings.requiredUpdateNotificationPeriod.toLong()
        workManager.enqueueUniqueWork(
            "ForceUpdateNotificationWorker",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequest.Builder(ForceUpdateNotificationWorker::class.java)
                .setInitialDelay(
                    if (withDelay) delay else 0,
                    TimeUnit.SECONDS
                )
                .build()
        )
    }

    fun scheduleInitialDiagnosisKeysRequest() {
        enqueueDiagnosisKeysRequest(
            ExistingWorkPolicy.KEEP,
            delayMinutes = SecureRandom().nextInt(2 * 60).toLong()
        )
    }

    fun scheduleNextDiagnosisKeysRequest() {
        enqueueDiagnosisKeysRequest(
            ExistingWorkPolicy.REPLACE,
            delayMinutes = settings.exposureDetectionPeriod.toLong() / 60
        )
    }

    fun scheduleDebugDiagnosisKeysRequest() {
        enqueueDiagnosisKeysRequest(ExistingWorkPolicy.REPLACE)
    }

    private fun enqueueDiagnosisKeysRequest(policy: ExistingWorkPolicy, delayMinutes: Long = 0) {
        workManager.enqueueUniqueWork(
            "RequestDiagnosisKeysWorker",
            policy,
            OneTimeWorkRequest.Builder(RequestDiagnosisKeysWorker::class.java)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        )
    }

    fun scheduleNextDummyExposureIngestionWorker(policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE) {
        workManager.enqueueUniqueWork(
            "NextDummyExposureIngestionWorker",
            policy,
            OneTimeWorkRequest.Builder(RequestDiagnosisKeysWorker::class.java)
                .setInitialDelay(computeNextDummyExposureIngestionScheduleDelay(), TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        )
    }

    private fun computeNextDummyExposureIngestionScheduleDelay(): Long {
        return SecureRandom().exponential(settings.dummyTeksAverageOpportunityWaitingTime.toLong())
    }
}
