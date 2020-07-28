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

package it.ministerodellasalute.immuni.ui.onboarding.fragments.viewpager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.OnboardingNavDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import kotlinx.android.synthetic.main.onboarding_notifications_fragment.*

class NotificationsFragment :
    ViewPagerBaseFragment(R.layout.onboarding_notifications_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        next.isEnabled = true

        next.setSafeOnClickListener {
            if (canProceed()) {
                viewModel.onNextTap()
            } else {
                val action = OnboardingNavDirections.actionNotificationSteps()
                findNavController().navigate(action)
            }
        }

        setupImage(R.raw.lottie_phones_07, R.drawable.ic_onboarding_notifications)
        checkSpacing()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background))
        // Auto-Skip if enabled
        if (canProceed()) {
            viewModel.onNextTap()
        }
    }

    private fun canProceed(): Boolean {
        return viewModel.pushNotificationManager.areNotificationsEnabled()
    }
}
