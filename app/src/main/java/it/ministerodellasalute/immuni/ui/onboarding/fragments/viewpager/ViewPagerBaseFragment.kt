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
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.extensions.utils.isTopEndDevice
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.onboarding.OnboardingViewModel
import it.ministerodellasalute.immuni.ui.onboarding.fragments.ViewPagerFragmentDirections
import it.ministerodellasalute.immuni.util.GlideApp
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

abstract class ViewPagerBaseFragment(@LayoutRes val layout: Int) : Fragment(layout) {

    protected lateinit var viewModel: OnboardingViewModel
    private val animationView: LottieAnimationView? by lazy { requireView().findViewById(R.id.image) as? LottieAnimationView }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()

        view.findViewById<TextView>(R.id.knowMore)?.setSafeOnClickListener {
            val action = ViewPagerFragmentDirections.actionHowitworks(false)
            findNavController().navigate(action)
        }

        // pause animation while scrolling
        view.findViewById<NestedScrollView>(R.id.scrollView)?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) resumeAnimations()
            else if (event.action == MotionEvent.ACTION_DOWN) pauseAnimations()
            false
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

    /**
     * Sets animation resource if high-end device, otherwise fallbacks into static drawable image.
     */
    protected fun setupImage(@RawRes animationResId: Int, @DrawableRes fallbackImageResId: Int) {
        val animationView = this.animationView ?: return

        if (isTopEndDevice(requireContext())) {
            animationView.apply {
                setAnimation(animationResId)
                loop(true)
                playAnimation()
            }
        } else {
            GlideApp
                .with(requireContext())
                .load(fallbackImageResId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(animationView)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseAnimations()
    }

    override fun onResume() {
        super.onResume()
        resumeAnimations()
    }

    fun pauseAnimations() {
        animationView?.pauseAnimation()
    }

    fun resumeAnimations() {
        animationView?.resumeAnimation()
    }
}
