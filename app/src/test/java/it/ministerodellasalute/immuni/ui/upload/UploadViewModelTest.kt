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

package it.ministerodellasalute.immuni.ui.upload

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.extensions.test.getValueForTest
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.OtpToken
import it.ministerodellasalute.immuni.testutils.CoroutinesTestRule
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UploadViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UploadViewModel

    @MockK(relaxed = true)
    lateinit var activity: Activity

    @MockK(relaxed = true)
    lateinit var exposureManager: ExposureManager

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        viewModel = UploadViewModel(exposureManager)
    }

    @Test
    fun `upload handles error`() = coroutineTestRule.runBlockingTest {
        coEvery { exposureManager.uploadTeks(activity, otpToken, null) } returns false
        // act
        viewModel.upload(activity, otpToken, null)
        // assert
        advanceUntilIdle() // bypass delay
        assertNull(viewModel.uploadSuccess.getValueForTest())
        assertNotNull(viewModel.uploadError.getValueForTest())
        assertEquals(false, viewModel.loading.getValueForTest())
    }

    @Test
    fun `upload success`() = coroutineTestRule.runBlockingTest {
        // arrange
        coEvery { exposureManager.uploadTeks(activity, otpToken, null) } returns true
        // act
        viewModel.upload(activity, otpToken, null)
        // assert
        advanceUntilIdle() // bypass delay
        assertNull(viewModel.uploadError.getValueForTest())
        assertNotNull(viewModel.uploadSuccess.getValueForTest())
        assertEquals(false, viewModel.loading.getValueForTest())
    }

    companion object {
        const val rawOtpCode = "OTP1234COD"
        val otpToken = OtpToken(rawOtpCode, Date())
    }
}
