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

package it.ministerodellasalute.immuni.ui.otp

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import it.ministerodellasalute.immuni.extensions.test.getOrAwaitValue
import it.ministerodellasalute.immuni.extensions.test.getValueForTest
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.OtpToken
import it.ministerodellasalute.immuni.logic.exposure.models.OtpValidationResult
import it.ministerodellasalute.immuni.logic.upload.OtpGenerator
import it.ministerodellasalute.immuni.logic.upload.UploadDisabler
import it.ministerodellasalute.immuni.testutils.CoroutinesTestRule
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OtpViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: OtpViewModel

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var uploadDisableManager: UploadDisabler

    @MockK(relaxed = false)
    lateinit var otpGenerator: OtpGenerator

    @MockK(relaxed = true)
    lateinit var exposureManager: ExposureManager

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    private fun initViewModel() {
        // This must be put here, since Otp generation is run at initialization
        every { otpGenerator.nextOtpCode() } returns (rawOtpCode)
        every { otpGenerator.prettify(rawOtpCode, " ") } returns prettyOtpCode
        viewModel = OtpViewModel(context, uploadDisableManager, otpGenerator, exposureManager)
    }

    @Test
    fun `generates otp code initially`() = coroutineTestRule.runBlockingTest {
        initViewModel()
        assertEquals(prettyOtpCode, viewModel.otpCode.getOrAwaitValue())
    }

    @Test
    fun `verification button disabling then unlocked`() = coroutineTestRule.runBlockingTest {
        val disabledForSeconds = 10L
        val disableSecondsFlow = flow {
            emit(disabledForSeconds)
            delay(1000)
            // expired
            emit(null)
        }
        every { uploadDisableManager.disabledForSecondsFlow } returns disableSecondsFlow
        initViewModel()

        assertNotNull(viewModel.buttonDisabledMessage.getValueForTest())
        advanceTimeBy(1000)
        assertNull(viewModel.buttonDisabledMessage.getValueForTest())
    }

    @Test
    fun `unauthorized verification submits failed attempt`() = coroutineTestRule.runBlockingTest {
        coEvery { exposureManager.validateOtp(rawOtpCode) } returns OtpValidationResult.Unauthorized

        initViewModel()
        viewModel.verify()
        advanceUntilIdle()
        assertNotNull(viewModel.verificationError.getValueForTest())
        verify(exactly = 1) { uploadDisableManager.submitFailedAttempt() }
    }

    @Test
    fun `otp verification resets updateDisabler and navigates`() =
        coroutineTestRule.runBlockingTest {
            coEvery { exposureManager.validateOtp(rawOtpCode) } returns OtpValidationResult.Success(
                OtpToken(rawOtpCode, Date())
            )

            initViewModel()
            viewModel.verify()
            advanceUntilIdle()
            assertNull(viewModel.verificationError.getValueForTest())
            verify(exactly = 0) { uploadDisableManager.submitFailedAttempt() }
            verify { uploadDisableManager.reset() }
            val navEvent = viewModel.navigateToUploadPage.getValueForTest()
            assertNotNull(navEvent)
            assertEquals(rawOtpCode, navEvent.peekContent().otp)
        }

    companion object {
        const val rawOtpCode = "OTP1234COD"
        const val prettyOtpCode = "OTP-1234-COD"
    }
}
