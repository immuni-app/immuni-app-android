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

package it.ministerodellasalute.immuni.repositories

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.logic.upload.UploadDisabler
import it.ministerodellasalute.immuni.logic.upload.UploadDisablerStore
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UploadDisableManagerTest {

    @MockK(relaxed = true)
    lateinit var uploadDisablerStore: UploadDisablerStore
    lateinit var uploadDisableManager: UploadDisabler

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        uploadDisableManager =
            UploadDisabler(
                uploadDisablerStore
            )
    }

    @Test
    fun `initially always emits null`() = runBlocking {
        uploadDisableManager.disabledForSecondsFlow.take(1).collect {
            assertNull(it)
        }
    }

    @Test
    fun `disables 5 seconds on first failed attempt`() = runBlocking {
        every { uploadDisablerStore.lastFailedAttemptTime } returnsMany listOf(null, Date())
        every { uploadDisablerStore.numFailedAttempts } returnsMany listOf(null, 1)

        uploadDisableManager.submitFailedAttempt()
        uploadDisableManager.disabledForSecondsFlow.take(1).collect {
            assertNotNull(it)
            assertEquals(5, it)
        }
    }

    @Test
    fun `disables 10 seconds on first failed attempt`() = runBlocking {
        every { uploadDisablerStore.lastFailedAttemptTime } returnsMany listOf(null, Date())
        every { uploadDisablerStore.numFailedAttempts } returnsMany listOf(null, 2)
        // Two failed attempts
        uploadDisableManager.submitFailedAttempt()
        uploadDisableManager.submitFailedAttempt()

        uploadDisableManager.disabledForSecondsFlow.take(1).collect {
            assertNotNull(it)
            assertEquals(10, it)
        }
    }

    @Test
    fun `resets disable`() = runBlocking {
        every { uploadDisablerStore.lastFailedAttemptTime } returnsMany listOf(null, Date(), null)
        every { uploadDisablerStore.numFailedAttempts } returnsMany listOf(null, 2, null)
        // Two failed attempts
        uploadDisableManager.submitFailedAttempt()
        uploadDisableManager.submitFailedAttempt()
        uploadDisableManager.reset()

        uploadDisableManager.disabledForSecondsFlow.take(1).collect {
            assertNull(it)
        }
    }
}
