package org.immuni.android.setup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlin.test.assertTrue
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.data.SettingsRepository
import org.immuni.android.extensions.test.getOrAwaitValue
import org.immuni.android.managers.UserManager
import org.immuni.android.network.api.NetworkError
import org.immuni.android.network.api.NetworkResource
import org.immuni.android.testutils.CoroutineTestRule
import org.immuni.android.ui.setup.SetupViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    lateinit var userManager: UserManager
    @MockK(relaxed = true)
    lateinit var repository: SettingsRepository

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { userManager.familyMembers() } returns listOf()
        viewModel = SetupViewModel(userManager, repository)
    }

    @Test
    fun `test setup fails if settings fails`() = coroutineTestRule.runBlockingTest {

        every { userManager.isSetupComplete() } returns false
        coEvery { repository.fetchSettings() } returns NetworkResource.Error(NetworkError.IOError())

        viewModel.initializeApp()

        assertTrue(viewModel.errorDuringSetup.getOrAwaitValue())
    }

    @Test
    fun `test navigate to welcome if first opening`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete() } returns false
        every { userManager.isOnboardingComplete() } returns false
        every { userManager.isWelcomeComplete() } returns false
        coEvery { repository.fetchSettings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()

        assertTrue(viewModel.navigateToWelcome.getOrAwaitValue().peekContent())
    }

    @Test
    fun `test navigate to home if not first opening`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete() } returns false
        every { userManager.isOnboardingComplete() } returns true
        every { userManager.isWelcomeComplete() } returns true
        coEvery { repository.fetchSettings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()

        assertTrue(viewModel.navigateToMainPage.getOrAwaitValue().peekContent())
    }

    @Test
    fun `test if not first setup but never did welcome navigate to welcome`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete() } returns true
        every { userManager.isOnboardingComplete() } returns false
        every { userManager.isWelcomeComplete() } returns false
        coEvery { repository.fetchSettings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()
        advanceTimeBy(2000)

        assertTrue(viewModel.navigateToWelcome.getOrAwaitValue().peekContent())
    }

    @Test
    fun `test if not first setup and did welcome navigate to home`() = coroutineTestRule.runBlockingTest {
        every { userManager.isSetupComplete() } returns true
        every { userManager.isOnboardingComplete() } returns true
        every { userManager.isWelcomeComplete() } returns true
        coEvery { repository.fetchSettings() } returns NetworkResource.Success(ImmuniSettings())

        viewModel.initializeApp()
        advanceTimeBy(2000)

        assertTrue(viewModel.navigateToMainPage.getOrAwaitValue().peekContent())
    }
}
