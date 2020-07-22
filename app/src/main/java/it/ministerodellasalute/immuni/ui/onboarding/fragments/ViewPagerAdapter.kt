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

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.ministerodellasalute.immuni.ui.onboarding.fragments.viewpager.*
import org.koin.core.KoinComponent

class ViewPagerAdapter(
    fragment: Fragment,
    isOnboardingComplete: Boolean,
    isBroadcastingActive: Boolean,
    areNotificationsEnabled: Boolean,
    isEditingProvince: Boolean,
    experimentalPhase: Boolean
) : FragmentStateAdapter(fragment), KoinComponent {

    private val items: MutableList<Type> = mutableListOf()

    init {
        if (isEditingProvince && isOnboardingComplete) {
            items.add(Type.REGION)
            items.add(Type.PROVINCE)
        } else {
            if (!isOnboardingComplete) {
                items.add(Type.PRIVACY)
                items.add(Type.REGION)
                items.add(Type.PROVINCE)
            }
            if (!isBroadcastingActive) {
                items.add(Type.EXPOSURE_NOTIFICATION)
            }
            if (!areNotificationsEnabled) {
                items.add(Type.NOTIFICATIONS)
            }
            if (!isOnboardingComplete) {
                items.add(Type.PROTECT_DEVICE)
                items.add(Type.PHISHING_WARNING)
                if (experimentalPhase) {
                    items.add(Type.PILOT_PROJECT)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val fragment = when (items[position]) {
            Type.EXPOSURE_NOTIFICATION -> ExposureNotificationFragment()
            Type.PRIVACY -> PrivacyFragment()
            Type.BLUETOOTH -> BluetoothFragment()
            Type.REGION -> RegionFragment()
            Type.PROVINCE -> ProvinceFragment()
            Type.NOTIFICATIONS -> NotificationsFragment()
            Type.PROTECT_DEVICE -> ProtectDeviceFragment()
            Type.PHISHING_WARNING -> PhishingWarningFragment()
            Type.PILOT_PROJECT -> PilotProjectFragment()
        }

        return fragment.apply {
            arguments = bundleOf("position" to position)
        }
    }

    private enum class Type {
        REGION, PROVINCE, PRIVACY, BLUETOOTH, EXPOSURE_NOTIFICATION, NOTIFICATIONS, PROTECT_DEVICE, PHISHING_WARNING, PILOT_PROJECT
    }
}
