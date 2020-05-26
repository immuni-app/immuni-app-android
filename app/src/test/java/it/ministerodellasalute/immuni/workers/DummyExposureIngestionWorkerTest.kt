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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class DummyExposureIngestionWorkerTest {
    @MockK
    private lateinit var workerManager: WorkerManager
    @MockK
    private lateinit var exposureManager: ExposureManager
    @MockK
    private lateinit var appLifecycleObserver: AppLifecycleObserver

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `when app is active, the work is canceled and rescheduled`() = runBlocking {
        every { appLifecycleObserver.isInForeground } returns MutableStateFlow(true)

        val worker = spyk(DummyExposureIngestionWorker.Impl(
            configuration = DummyExposureIngestionWorker.Configuration(
                teksAverageRequestWaitingTime = 1,
                teksRequestProbabilities = listOf()
            ),
            appLifecycleObserver = AppLifecycleObserver(),
            workerManager = workerManager,
            exposureManager = exposureManager
        ))

        worker.doWork()
    }

    @Test
    fun `when app becomes active, the work is canceled and rescheduled`() {

    }

    @Test
    fun `with probability=0, the upload is performed exactly once`() {

    }

    @Test
    fun `with 2 slots of probability=1, the upload is performed exactly three times`() {

    }
}
