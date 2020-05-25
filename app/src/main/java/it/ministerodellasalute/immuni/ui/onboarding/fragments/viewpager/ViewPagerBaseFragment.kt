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
import it.ministerodellasalute.immuni.extensions.utils.log
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.visible
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
     * Hide illustration when there is not enough space on top.
     * Use a maximum aspect ratio.
     */
    fun checkSpacing() {
        view?.findViewById<TextView>(R.id.title)?.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                view?.findViewById<TextView>(R.id.title)?.removeOnLayoutChangeListener(this)
                val w = ScreenUtils.getScreenWidth(requireContext())
                val aspectRatio = w.toFloat() / top.toFloat()
                log("aspectRatio $aspectRatio")
                if (aspectRatio > 2) {
                    view?.findViewById<View>(R.id.image)?.gone()
                } else {
                    view?.findViewById<View>(R.id.image)?.visible()
                }
            }
        })
    }
}
