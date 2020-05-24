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

import androidx.work.ExistingWorkPolicy
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import it.ministerodellasalute.immuni.logic.forceupdate.PlayServicesStatus
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import org.junit.Before
import org.junit.Test

class OnboardingNotCompletedWorkerTest {
    @MockK(relaxed = true)
    private lateinit var notificationManager: AppNotificationManager
    @MockK(relaxed = true)
    private lateinit var workerManager: WorkerManager

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `triggers the notification when onboarding is not complete and EN is available`() {
        OnboardingNotCompletedWorker.doWork(
            isOnboardingComplete = false,
            playServicesStatus = { PlayServicesStatus.AVAILABLE },
            notificationManager = notificationManager,
            workerManager = workerManager
        )

        verify { notificationManager.triggerNotification(NotificationType.OnboardingNotCompleted) }
        verify(inverse = true) { workerManager.scheduleOnboardingNotCompletedWorker() }
    }

    @Test
    fun `triggers the notification when onboarding is not complete and EN is update required`() {
        OnboardingNotCompletedWorker.doWork(
            isOnboardingComplete = false,
            playServicesStatus = { PlayServicesStatus.UPDATE_REQUIRED },
            notificationManager = notificationManager,
            workerManager = workerManager
        )

        verify { notificationManager.triggerNotification(NotificationType.OnboardingNotCompleted) }
        verify(inverse = true) { workerManager.scheduleOnboardingNotCompletedWorker() }
    }

    @Test
    fun `reschedules the worker but does not trigger the notification when onboarding is not complete and EN is not available`() {
        OnboardingNotCompletedWorker.doWork(
            isOnboardingComplete = false,
            playServicesStatus = { PlayServicesStatus.NOT_AVAILABLE },
            notificationManager = notificationManager,
            workerManager = workerManager
        )

        verify(inverse = true) { notificationManager.triggerNotification(NotificationType.OnboardingNotCompleted) }
        verify { workerManager.scheduleOnboardingNotCompletedWorker(ExistingWorkPolicy.REPLACE) }
    }

    @Test
    fun `does not reschedule the worker if onboarding is complete and does not trigger notification`() {
        OnboardingNotCompletedWorker.doWork(
            isOnboardingComplete = true,
            playServicesStatus = { PlayServicesStatus.AVAILABLE },
            notificationManager = notificationManager,
            workerManager = workerManager
        )

        verify(inverse = true) { notificationManager.triggerNotification(NotificationType.OnboardingNotCompleted) }
        verify(inverse = true) { workerManager.scheduleOnboardingNotCompletedWorker() }
    }
}
