package org.immuni.android.setup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.immuni.android.managers.UserManager
import org.immuni.android.testutils.CoroutineTestRule
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.setup.SetupRepository
import org.immuni.android.ui.setup.SetupViewModel
import org.immuni.android.ui.welcome.Welcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import io.mockk.impl.annotations.MockK
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import retrofit2.Response
import kotlin.test.assertTrue
import kotlin.test.fail

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
    lateinit var repository : SetupRepository

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        viewModel = SetupViewModel(setup, onboarding, welcome, userManager, repository)
    }

    @Test
    fun `test setup fails if settings fails`() = runBlockingTest {

        every { setup.isComplete() } returns false
        coEvery { repository.getOracleSetting() } returns Response.error(500, "".toResponseBody())
        coEvery { repository.getOracleMe() } returns Response.success(ImmuniMe())

        viewModel.initializeApp()

        viewModel.errorDuringSetup.observeForever { error ->
            assertTrue(error)
        }
    }

    @Test
    fun `test setup fails if me fails`() = runBlockingTest {
        every { setup.isComplete() } returns false
        coEvery { repository.getOracleSetting() } returns Response.success(ImmuniSettings())
        coEvery { repository.getOracleMe() } returns Response.error(500, "".toResponseBody())

        viewModel.initializeApp()

        viewModel.errorDuringSetup.observeForever { error ->
            assertTrue(error)
        }
    }

    @Test
    fun `test navigate to welcome if first opening`() = runBlockingTest {
        fail("TODO")
    }

    @Test
    fun `test navigate to home if not first opening`() = runBlockingTest {
        fail("TODO")
    }

    @Test
    fun `test if not first setup but never did welcome navigate to welcome`() = runBlockingTest {
        fail("TODO")
    }

    @Test
    fun `test if not first setup and did welcome navigate to home`() = runBlockingTest {
        fail("TODO")
    }
}