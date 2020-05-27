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
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.onboarding.OnboardingViewModel
import it.ministerodellasalute.immuni.ui.onboarding.fragments.ViewPagerFragmentDirections
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class ViewPagerBaseFragment(@LayoutRes val layout: Int) : Fragment(layout) {

    protected lateinit var viewModel: OnboardingViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()

        view.findViewById<TextView>(R.id.knowMore)?.setSafeOnClickListener {
            val action = ViewPagerFragmentDirections.actionHowitworks(false)
            findNavController().navigate(action)
        }
    }

    /**
     * Illustration can never be more than 50% of the screen height.
     * So that there is enough space for the text and then scroll.
     */
    fun checkSpacing() {
        val image = view?.findViewById<View>(R.id.image)
        image?.let { view ->
            var params = view.layoutParams
            params.height = (ScreenUtils.getScreenHeight(requireContext()).toFloat() / 2f).toInt()
            view.layoutParams = params
        }
    }
}
