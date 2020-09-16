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
import androidx.work.*
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.exponential
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.workers.*
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import org.koin.core.KoinComponent

class WorkerManager(
    private val context: Context,
    private val settingsManager: ConfigurationSettingsManager,
    private val notificationManager: AppNotificationManager,
    private val workManager: WorkManager
) : KoinComponent {
    constructor(
        context: Context,
        settingsManager: ConfigurationSettingsManager,
        notificationManager: AppNotificationManager
    ) : this(
        context = context,
        settingsManager = settingsManager,
        notificationManager = notificationManager,
        workManager = WorkManager.getInstance(context)
    )

    private val settings get() = settingsManager.settings.value

    fun scheduleOnboardingNotCompletedWorker(policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE) {
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

    fun updateForceUpdateNotificationWorkerSchedule() {
        if (settingsManager.isAppOutdated) {
            scheduleForceUpdateNotificationWorker(withDelay = false)
        } else {
            notificationManager.removeNotification(NotificationType.ForcedVersionUpdate)
        }
    }

    fun scheduleForceUpdateNotificationWorker(withDelay: Boolean = true) {
        val delay = settings.requiredUpdateNotificationPeriod.toLong()
        workManager.enqueueUniqueWork(
            "ForceUpdateNotificationWorker",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(ForceUpdateNotificationWorker::class.java)
                .setInitialDelay(
                    if (withDelay) delay else 0,
                    TimeUnit.SECONDS
                )
                .build()
        )
    }

    fun scheduleNotificationsCleanerWorker(withDelay: Boolean = true) {

        val delay = when (context.resources.getBoolean(R.bool.development_device)) {
            true -> 10L // 10 seconds
            else -> 60 * 15L // 15 minutes
        }

        workManager.enqueueUniqueWork(
            "NotificationsCleanerWorker",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(NotificationsCleanerWorker::class.java)
                .setInitialDelay(
                    if (withDelay) delay else 0,
                    TimeUnit.SECONDS
                )
                .build()
        )
    }

    fun scheduleServiceNotActiveNotificationWorker(policy: ExistingWorkPolicy) {
        workManager.enqueueUniqueWork(
            "ServiceNotActiveNotificationWorker",
            policy,
            OneTimeWorkRequest.Builder(ServiceNotActiveNotificationWorker::class.java)
                .setInitialDelay(
                    15,
                    TimeUnit.MINUTES
                )
                .build()
        )
    }

    fun updateRiskReminderWorker(exposureStatus: ExposureStatus) {
        if (exposureStatus is ExposureStatus.Exposed && !exposureStatus.acknowledged) {
            scheduleRiskReminderWorker(ExistingWorkPolicy.REPLACE)
        } else {
            cancelRiskReminderWorker()
        }
    }

    fun scheduleRiskReminderWorker(policy: ExistingWorkPolicy) {
        val delay = settings.riskReminderNotificationPeriod.toLong()
        workManager.enqueueUniqueWork(
            "RiskReminderWorker",
            policy,
            OneTimeWorkRequest.Builder(RiskReminderWorker::class.java)
                .setInitialDelay(
                    delay,
                    TimeUnit.SECONDS
                )
                .build()
        )
    }

    private fun cancelRiskReminderWorker() {
        workManager.cancelUniqueWork("RiskReminderWorker")
    }

    fun scheduleInitialDiagnosisKeysRequest() {
        // Use a unique work to avoid multiple workers.
        // Use ExistingPeriodicWorkPolicy.REPLACE to ensure that the job is rescheduled
        // as a workaround for https://issuetracker.google.com/166292069.
        enqueueDiagnosisKeysRequest(
            ExistingWorkPolicy.REPLACE,
            delayMinutes = 0
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
                        .build()
                )
                .build()
        )
    }

    fun scheduleNextDummyExposureIngestionWorker(policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE) {
        workManager.enqueueUniqueWork(
            "NextDummyExposureIngestionWorker",
            policy,
            OneTimeWorkRequest.Builder(DummyExposureIngestionWorker::class.java)
                .setInitialDelay(computeNextDummyExposureIngestionScheduleDelay(), TimeUnit.SECONDS)
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

    fun scheduleExposureAnalyticsWorker(serverDate: Date) {
        workManager.enqueueUniqueWork(
            "ExposureAnalyticsWorker",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Builder(ExposureAnalyticsWorker::class.java)
                .setInitialDelay(10, TimeUnit.MINUTES)
                .setInputData(workDataOf(ExposureAnalyticsWorker.SERVER_DATE_INPUT_DATA_KEY to serverDate.time))
                .build()
        )
    }
}
