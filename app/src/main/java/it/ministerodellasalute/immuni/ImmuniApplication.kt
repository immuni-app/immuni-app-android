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
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.repositories.UserRepository
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
import org.koin.core.inject

class ImmuniApplication : Application(), KoinComponent {

    private lateinit var settingsManager: ConfigurationSettingsManager
    private lateinit var forceUpdateManager: ForceUpdateManager
    private lateinit var exposureManager: ExposureManager
    private lateinit var debugMenu: DebugMenu
    private lateinit var lifecycleObserver: AppLifecycleObserver
    private lateinit var activityLifecycleObserver: AppActivityLifecycleCallbacks
    private val userRepository: UserRepository by inject()
    private val workerManager: WorkerManager by inject()

    override fun onCreate() {
        super.onCreate()

        // start Koin DI module
        startKoin {
            androidLogger()
            androidContext(this@ImmuniApplication)
            modules(appModule)
        }

        // we need to instantiate this objects immediately
        debugMenu = get()
        settingsManager = get()
        forceUpdateManager = get()
        exposureManager = get()

        // register app lifecycle
        lifecycleObserver = get()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // register activity lifecycle
        activityLifecycleObserver = get()
        registerActivityLifecycleCallbacks(activityLifecycleObserver)

        startWorkers()
    }

    private fun startWorkers() {
        workerManager.scheduleNextDummyExposureIngestionWorker(ExistingWorkPolicy.KEEP)
        scheduleOnboardingNotCompletedWorker()
        scheduleServiceNotActiveNotificationWorker()
    }

    private fun scheduleOnboardingNotCompletedWorker() {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)
        lifecycleObserver.isInForeground.onEach { isInForeground ->
            if (!userRepository.isOnboardingComplete.value) {
                if (isInForeground) {
                    workerManager.scheduleOnboardingNotCompletedWorker()
                }
            } else {
                job.cancel()
            }
        }.launchIn(scope)
    }

    private fun scheduleServiceNotActiveNotificationWorker() {
        workerManager.scheduleServiceNotActiveNotificationWorker(ExistingWorkPolicy.KEEP)

        val scope = CoroutineScope(Dispatchers.Default)
        // When isBroadcastingActive becomes false, it means that the user manually disabled BLE or Location Services
        // and so already received a notification from Google Play Services with that specific warning.
        // We reschedule the worker, so that our custom notification will be triggered only if the user still didn't fix the issue
        // by the time the Work is run again.
        exposureManager.isBroadcastingActive.filter { it == false }.onEach {
            workerManager.scheduleServiceNotActiveNotificationWorker(ExistingWorkPolicy.REPLACE)
        }.launchIn(scope)
    }
}
