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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.onboarding.fragments.ViewPagerFragmentDirections
import kotlinx.android.synthetic.main.onboarding_exposure_fragment.*

class ExposureNotificationFragment :
    ViewPagerBaseFragment(R.layout.onboarding_exposure_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var handled = false
        viewModel.isBroadcastingActive.observe(viewLifecycleOwner, Observer { isBroadcastingActive ->
            if (!handled && isBroadcastingActive == true && this.isResumed) {
                handled = true
                navigateNext()
            }
        })

        viewModel.googlePlayServicesError.observe(viewLifecycleOwner, Observer { pair ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(pair.first)
                .setMessage(pair.second)
                .setPositiveButton(getString(R.string.onboarding_pin_advice_action)) { d, _ ->
                    d.dismiss()
                }
                .show()
        })

        next.isEnabled = true

        next.setOnClickListener(null)
        next.setSafeOnClickListener {

            if (canProceed()) {
                navigateNext()
            } else {
                activity?.let {
                    viewModel.startExposureNotification(it)
                }
            }
        }

        knowMore.setSafeOnClickListener {
            // EN on Android 11 don't require active location
            if (viewModel.deviceSupportsLocationlessScanning) {
                val action = ViewPagerFragmentDirections.actionHowitworks(false)
                findNavController().navigate(action)
            } else {
                val action = ViewPagerFragmentDirections.actionLocalisationExplanation()
                findNavController().navigate(action)
            }
        }

        setupImage(R.raw.lottie_notifications_05, R.drawable.ic_onboarding_exposure)
        checkSpacing()
        checkLocalisationRequired()
    }

    private fun checkLocalisationRequired() {
        // EN on Android 11 don't require active location
        if (viewModel.deviceSupportsLocationlessScanning) {
            extendedMessage.gone()
        }
    }

    private fun navigateNext() {
        viewModel.onNextTap()
    }

    private fun canProceed(): Boolean {
        return viewModel.isBroadcastingActive.value ?: false
    }
}
