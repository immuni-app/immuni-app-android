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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.workers.models.ServiceNotActiveNotificationWorkerStatus
import it.ministerodellasalute.immuni.workers.repositories.ServiceNotActiveNotificationWorkerRepository
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class ServiceNotActiveNotificationWorkerTest {
    companion object {
        const val ONE_DAY = 24 * 60 * 60
    }

    @MockK(relaxUnitFun = true)
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var exposureManager: ExposureManager

    @MockK(relaxUnitFun = true)
    private lateinit var repository: ServiceNotActiveNotificationWorkerRepository

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `if broadcasting is active the status is set to Working`() = runBlockingTest {
        coEvery { exposureManager.updateAndGetServiceIsActive() } returns true
        every { exposureManager.isBroadcastingActive.value } returns true
        every { repository.status } returns null

        doWork(Date())

        verify(exactly = 1) {
            repository.status = ServiceNotActiveNotificationWorkerStatus.Working()
        }
        verify(exactly = 0) {
            notificationManager.triggerNotification(NotificationType.ServiceNotActive)
        }
    }

    @Test
    fun `if broadcasting is not active and service is active, notification is sent after 1 day`() {
        return runBlockingTest {
            coEvery { exposureManager.updateAndGetServiceIsActive() } returns true
            every { exposureManager.isBroadcastingActive.value } returns false
            var status: ServiceNotActiveNotificationWorkerStatus =
                ServiceNotActiveNotificationWorkerStatus.Working()
            every { repository.status } answers { status }
            every {
                repository.status = any()
            } propertyType ServiceNotActiveNotificationWorkerStatus::class answers {
                status = value
            }

            val currentDate = Date()
            doWork(currentDate)

            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
            }
            verify(exactly = 0) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }

            val sixHoursLater = Date(currentDate.time + 6 * 60 * 60 * 1000)
            doWork(sixHoursLater)
            verify(exactly = 0) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }
            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
            }
            verify(exactly = 0) {
                repository.status =
                    ServiceNotActiveNotificationWorkerStatus.NotWorking(sixHoursLater)
            }

            val tomorrow = Date(currentDate.time + 24 * 60 * 60 * 1000)
            doWork(tomorrow)

            verify(exactly = 1) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }
            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
            }
            verify(exactly = 0) {
                repository.status =
                    ServiceNotActiveNotificationWorkerStatus.NotWorking(sixHoursLater)
            }
            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(tomorrow)
            }
        }
    }

    @Test
    fun `if broadcasting is not active and service is not active, notification is sent now and after 1 day`() {
        return runBlockingTest {
            coEvery { exposureManager.updateAndGetServiceIsActive() } returns false
            every { exposureManager.isBroadcastingActive.value } returns false
            var status: ServiceNotActiveNotificationWorkerStatus =
                ServiceNotActiveNotificationWorkerStatus.Working()
            every { repository.status } answers { status }
            every {
                repository.status = any()
            } propertyType ServiceNotActiveNotificationWorkerStatus::class answers {
                status = value
            }

            val currentDate = Date()
            doWork(currentDate)

            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
            }
            verify(exactly = 1) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }

            val sixHoursLater = Date(currentDate.time + 6 * 60 * 60 * 1000)
            doWork(sixHoursLater)
            verify(exactly = 1) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }
            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
            }
            verify(exactly = 0) {
                repository.status =
                    ServiceNotActiveNotificationWorkerStatus.NotWorking(sixHoursLater)
            }

            val tomorrow = Date(currentDate.time + 24 * 60 * 60 * 1000)
            doWork(tomorrow)

            verify(exactly = 2) {
                notificationManager.triggerNotification(NotificationType.ServiceNotActive)
            }
            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(currentDate)
            }
            verify(exactly = 0) {
                repository.status =
                    ServiceNotActiveNotificationWorkerStatus.NotWorking(sixHoursLater)
            }
            verify(exactly = 1) {
                repository.status = ServiceNotActiveNotificationWorkerStatus.NotWorking(tomorrow)
            }
        }
    }

    private suspend fun doWork(currentDate: Date) {
        return ServiceNotActiveNotificationWorker.Impl(
            currentDate = currentDate,
            serviceNotActiveNotificationPeriod = ONE_DAY,
            exposureManager = exposureManager,
            repository = repository,
            notificationManager = notificationManager
        ).doWork()
    }
}
