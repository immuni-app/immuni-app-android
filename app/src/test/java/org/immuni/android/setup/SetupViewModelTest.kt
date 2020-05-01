package org.immuni.android.setup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import org.immuni.android.managers.UserManager
import org.immuni.android.testutils.CoroutineTestRule
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.setup.SetupViewModel
import org.immuni.android.ui.welcome.Welcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import io.mockk.impl.annotations.MockK
import org.immuni.android.api.APIManager
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.network.api.NetworkError
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.testutils.getOrAwaitValue
import kotlin.test.assertTrue

class SetupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    /**
     * class under test
     */
    lateinit var viewModel: SetupViewModel

    @MockK(relaxed = true)
    lateinit var setup : Setup
    @MockK(relaxed = true)
    lateinit var onboarding : Onboarding
    @MockK(relaxed = true)
    lateinit var welcome : Welcome
    @MockK(relaxed = true)
    lateinit var userManager : UserManager
    @MockK(relaxed = true)
    lateinit var apiManager : APIManager

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { userManager.familyMembers() } returns listOf()
        viewModel = SetupViewModel(setup, onboarding, welcome, userManager, apiManager)
    }

    @Test
    fun `test setup fails if settings fails`() = coroutineTestRule.runBlockingTest {

        every { setup.isComplete() } returns false
        coEvery { apiManager.repository.settings() } returns NetworkResource.Error(NetworkError.IOError())

        viewModel.initializeApp()

        assertTrue(viewModel.errorDuringSetup.getOrAwaitValue())
    }

    @Test
    fun `test navigate to welcome if first opening`() = coroutineTestRule.runBlockingTest {
        every { setup.isComplete() } returns false
        every { onboarding.isComplete() } returns false
        every { welcome.isComplete() } returns false
        coEvery { apiManager.repository.settings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()

        assertTrue(viewModel.navigateToWelcome.getOrAwaitValue().peekContent())
    }

    @Test
    fun `test navigate to home if not first opening`() = coroutineTestRule.runBlockingTest {
        every { setup.isComplete() } returns false
        every { onboarding.isComplete() } returns true
        every { welcome.isComplete() } returns true
        coEvery { apiManager.repository.settings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()

        assertTrue(viewModel.navigateToMainPage.getOrAwaitValue().peekContent())
    }

    @Test
    fun `test if not first setup but never did welcome navigate to welcome`() = coroutineTestRule.runBlockingTest {
        every { setup.isComplete() } returns true
        every { onboarding.isComplete() } returns false
        every { welcome.isComplete() } returns false
        coEvery { apiManager.repository.settings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()
        advanceTimeBy(2000)

        assertTrue(viewModel.navigateToWelcome.getOrAwaitValue().peekContent())
    }

    @Test
    fun `test if not first setup and did welcome navigate to home`() = coroutineTestRule.runBlockingTest {
        every { setup.isComplete() } returns true
        every { onboarding.isComplete() } returns true
        every { welcome.isComplete() } returns true
        coEvery { apiManager.repository.settings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()
        advanceTimeBy(2000)

        assertTrue(viewModel.navigateToMainPage.getOrAwaitValue().peekContent())
    }
}