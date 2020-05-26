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

package it.ministerodellasalute.immuni.ui.setup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.extensions.test.getOrAwaitValue
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.testutils.CoroutinesTestRule
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SetupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    /**
     * class under test
     */
    lateinit var viewModel: SetupViewModel

    @MockK(relaxed = true)
    lateinit var userManager: UserManager
    @MockK(relaxed = true)
    lateinit var settingsManager: ConfigurationSettingsManager

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        viewModel = SetupViewModel(userManager, settingsManager)
    }

    @Test
    fun `test navigate to welcome if first opening`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete.value } returns false
        every { userManager.isOnboardingComplete.value } returns false
        every { userManager.isWelcomeComplete.value } returns false

        viewModel.initializeApp()
        advanceTimeBy(4000)

        val destination = viewModel.navigationDestination.getOrAwaitValue().peekContent()
        assertEquals(SetupViewModel.Destination.Welcome, destination)
    }

    @Test
    fun `test navigate to home if not first opening`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete.value } returns true
        every { userManager.isOnboardingComplete.value } returns true
        every { userManager.isWelcomeComplete.value } returns true

        viewModel.initializeApp()
        advanceTimeBy(4000)

        val destination = viewModel.navigationDestination.getOrAwaitValue().peekContent()
        assertEquals(SetupViewModel.Destination.Home, destination)
    }

    @Test
    fun `test if not first setup but never did welcome navigate to welcome`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete.value } returns true
        every { userManager.isOnboardingComplete.value } returns false
        every { userManager.isWelcomeComplete.value } returns false

        viewModel.initializeApp()
        advanceTimeBy(4000)

        val destination = viewModel.navigationDestination.getOrAwaitValue().peekContent()
        assertEquals(SetupViewModel.Destination.Welcome, destination)
    }

    @Test
    fun `test if not first setup and did welcome navigate to home`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete.value } returns true
        every { userManager.isOnboardingComplete.value } returns true
        every { userManager.isWelcomeComplete.value } returns true

        viewModel.initializeApp()
        advanceTimeBy(4000)

        val destination = viewModel.navigationDestination.getOrAwaitValue().peekContent()
        assertEquals(SetupViewModel.Destination.Home, destination)
    }
}
