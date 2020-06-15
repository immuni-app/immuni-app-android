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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.isHighEndDevice
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.util.GlideApp
import kotlinx.android.synthetic.main.onboarding_phishing_warning.*

class PhishingWarningFragment : ViewPagerBaseFragment(R.layout.onboarding_phishing_warning) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        next.isEnabled = true

        next.setOnClickListener(null)
        next.setSafeOnClickListener {
            viewModel.onNextTap()
        }

        if (isHighEndDevice(requireContext())) {
            image.setAnimation(R.raw.lottie_man_10)
            image.loop(true)
            image.playAnimation()
        } else {
            GlideApp
                .with(requireContext())
                .load(R.drawable.ic_onboarding_phishing_warning)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image)
        }
        checkSpacing()
    }
}
