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

package it.ministerodellasalute.immuni.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import it.ministerodellasalute.immuni.api.services.Faq
import it.ministerodellasalute.immuni.extensions.test.getValueForTest
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.models.FetchFaqsResult
import it.ministerodellasalute.immuni.testutils.CoroutinesTestRule
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingsViewModel

    @MockK(relaxed = true)
    lateinit var settingsManager: ConfigurationSettingsManager

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        viewModel = SettingsViewModel(settingsManager)
    }

    @Test
    fun `navigate to FAQ if already cached`() = coroutineTestRule.runBlockingTest {
        coEvery { settingsManager.faqs.value } returns listOf(Faq("Title", "Content"))
        // act
        viewModel.onFaqClick()
        // assert
        advanceUntilIdle() // bypass delay
        assertNull(viewModel.errorFetchingFaqs.getValueForTest())
        assertEquals(true, viewModel.navigateToFaqs.getValueForTest()?.peekContent())
    }

    @Test
    fun `navigate to FAQ after fetching with success`() = coroutineTestRule.runBlockingTest {
        coEvery { settingsManager.faqs.value } returns null
        coEvery { settingsManager.fetchFaqsAsync() } returns CompletableDeferred(
            FetchFaqsResult.Success(
                listOf()
            )
        )
        // act
        viewModel.onFaqClick()
        // assert
        advanceUntilIdle() // bypass delay
        assertNull(viewModel.errorFetchingFaqs.getValueForTest())
        assertEquals(true, viewModel.navigateToFaqs.getValueForTest()?.peekContent())
    }

    @Test
    fun `shows error if fetching fails`() = coroutineTestRule.runBlockingTest {
        coEvery { settingsManager.faqs.value } returns null
        coEvery { settingsManager.fetchFaqsAsync() } returns CompletableDeferred<FetchFaqsResult.ConnectionError>()
        // act
        viewModel.onFaqClick()
        // assert
        advanceUntilIdle() // bypass delay
        assertNull(viewModel.navigateToFaqs.getValueForTest())
        assertEquals(true, viewModel.errorFetchingFaqs.getValueForTest()?.peekContent())
    }
}
