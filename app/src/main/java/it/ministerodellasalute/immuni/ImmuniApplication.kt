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

package it.ministerodellasalute.immuni

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingWorkPolicy
import it.ministerodellasalute.immuni.debugmenu.DebugMenu
import it.ministerodellasalute.immuni.extensions.lifecycle.AppActivityLifecycleCallbacks
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.extensions.utils.log
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ImmuniApplication : Application(), KoinComponent {

    private lateinit var settingsManager: ConfigurationSettingsManager
    private lateinit var forceUpdateManager: ForceUpdateManager
    private lateinit var exposureManager: ExposureManager
    private lateinit var debugMenu: DebugMenu
    private lateinit var lifecycleObserver: AppLifecycleObserver
    private lateinit var activityLifecycleObserver: AppActivityLifecycleCallbacks
    private lateinit var userManager: UserManager
    private lateinit var workerManager: WorkerManager

    override fun onCreate() {
        super.onCreate()

        // start Koin DI module
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@ImmuniApplication)
            modules(appModule)
        }

        // we need to instantiate this objects immediately
        debugMenu = get()
        settingsManager = get()
        forceUpdateManager = get()
        exposureManager = get()
        userManager = get()
        workerManager = get()

        // register app lifecycle
        lifecycleObserver = get()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // register activity lifecycle
        activityLifecycleObserver = get()
        registerActivityLifecycleCallbacks(activityLifecycleObserver)

        startWorkers()
    }

    private fun startWorkers() {
        updateNextDummyExposureIngestionWorker()
        updateOnboardingNotCompletedWorker()
        updateServiceNotActiveNotificationWorker()
        updateForceUpdateNotificationWorker()
        updateRiskReminderWorker()
        updateInitialDiagnosisKeysRequest()
        updateNotificationsCleanerWorker()
        log("Workers successfully started")
    }

    private fun updateNextDummyExposureIngestionWorker() {
        workerManager.scheduleNextDummyExposureIngestionWorker(ExistingWorkPolicy.KEEP)
    }

    private fun updateNotificationsCleanerWorker() {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)
        lifecycleObserver.isInForeground.filter { it }.onEach {
            workerManager.scheduleNotificationsCleanerWorker(withDelay = false)
        }.launchIn(scope)
    }

    private fun updateOnboardingNotCompletedWorker() {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)
        lifecycleObserver.isInForeground.onEach { isInForeground ->
            if (!userManager.isOnboardingComplete.value) {
                if (isInForeground) {
                    workerManager.scheduleOnboardingNotCompletedWorker()
                }
            } else {
                job.cancel()
            }
        }.launchIn(scope)
    }

    private fun updateServiceNotActiveNotificationWorker() {
        workerManager.scheduleServiceNotActiveNotificationWorker(ExistingWorkPolicy.KEEP)
    }

    private fun updateForceUpdateNotificationWorker() {
        val scope = CoroutineScope(Dispatchers.Default)

        settingsManager.settings.onEach {
            workerManager.updateForceUpdateNotificationWorkerSchedule()
        }.launchIn(scope)
    }

    private fun updateRiskReminderWorker() {
        val scope = CoroutineScope(Dispatchers.Default)

        exposureManager.exposureStatus.onEach {
            workerManager.updateRiskReminderWorker(it)
        }.launchIn(scope)
    }

    private fun updateInitialDiagnosisKeysRequest() {
        val scope = CoroutineScope(Dispatchers.Default)

        userManager.isOnboardingComplete.filter { it }.onEach {
            workerManager.scheduleInitialDiagnosisKeysRequest()
        }.launchIn(scope)
    }
}
