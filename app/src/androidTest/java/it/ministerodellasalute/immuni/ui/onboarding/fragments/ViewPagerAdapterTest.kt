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

package it.ministerodellasalute.immuni.ui.onboarding.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.ministerodellasalute.immuni.ui.onboarding.fragments.viewpager.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewPagerAdapterTest {

    private lateinit var adapter: ViewPagerAdapter

    @Test
    fun inEditingProvince_only_populates_province_region_fragments() {
        // Check for all possible params
        inEditingProvince_only_populates_province_region_fragments(
            areNotificationsEnabled = true,
            isBroadcastingActive = true
        )
        inEditingProvince_only_populates_province_region_fragments(
            areNotificationsEnabled = true,
            isBroadcastingActive = false
        )
        inEditingProvince_only_populates_province_region_fragments(
            areNotificationsEnabled = false,
            isBroadcastingActive = true
        )
        inEditingProvince_only_populates_province_region_fragments(
            areNotificationsEnabled = false,
            isBroadcastingActive = false
        )
    }

    private fun inEditingProvince_only_populates_province_region_fragments(
        areNotificationsEnabled: Boolean,
        isBroadcastingActive: Boolean
    ) {
        launchFragmentInContainer<Fragment>().onFragment {
            adapter = ViewPagerAdapter(
                it,
                isEditingProvince = true,
                isOnboardingComplete = true,
                areNotificationsEnabled = areNotificationsEnabled,
                isBroadcastingActive = isBroadcastingActive,
                experimentalPhase = false
            )

            assertEquals(2, adapter.itemCount)
            assertTrue(adapter.createFragment(0) is RegionFragment)
            assertTrue(adapter.createFragment(1) is ProvinceFragment)
        }
    }

    @Test
    fun whenOnBoardingIsComplete_skipOnBoarding() {
        listOf(true, false).forEach { areNotificationsEnabled ->
            listOf(true, false).forEach { isBroadcastingActive ->
                whenOnBoardingIsComplete_skipOnBoarding(
                    areNotificationsEnabled,
                    isBroadcastingActive
                )
            }
        }
    }

    private fun whenOnBoardingIsComplete_skipOnBoarding(
        areNotificationsEnabled: Boolean,
        isBroadcastingActive: Boolean
    ) {
        launchFragmentInContainer<Fragment>().onFragment { fragment ->
            adapter = ViewPagerAdapter(
                fragment,
                isEditingProvince = false,
                isOnboardingComplete = true,
                areNotificationsEnabled = areNotificationsEnabled,
                isBroadcastingActive = isBroadcastingActive,
                experimentalPhase = false
            )
            val adapterFragments = Array(adapter.itemCount) { adapter.createFragment(it) }
            assertTrue(adapterFragments.none { it is RegionFragment })
            assertTrue(adapterFragments.none { it is ProvinceFragment })
            assertTrue(adapterFragments.none { it is PrivacyFragment })
            assertTrue(adapterFragments.none { it is PhishingWarningFragment })
            assertTrue(adapterFragments.none { it is ProtectDeviceFragment })
        }
    }
}
