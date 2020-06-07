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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.ui.main.MainActivity
import it.ministerodellasalute.immuni.ui.onboarding.OnboardingViewModel
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import kotlinx.android.synthetic.main.onboarding_view_pager_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class ViewPagerFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.onboarding_view_pager_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = navArgs<ViewPagerFragmentArgs>()

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
        }

        with(viewPager) {
            adapter = with(viewModel) {
                ViewPagerAdapter(
                    fragment = this@ViewPagerFragment,
                    isOnboardingComplete = userManager.isOnboardingComplete.value,
                    isBroadcastingActive = exposureManager.isBroadcastingActive.value ?: false,
                    areNotificationsEnabled = pushNotificationManager.areNotificationsEnabled(),
                    isEditingProvince = args.value.isEditingProvince,
                    experimentalPhase = settingsManager.settings.value.experimentalPhase
                )
            }
            clipToPadding = false
            clipChildren = false
            isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        if ((viewPager.adapter?.itemCount ?: 0) == 0) activity?.finish()

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val newPos = viewPager.currentItem + 1
                if (newPos == (viewPager.adapter?.itemCount ?: 0)) {
                    navigateToDone()
                } else {
                    viewPager.setCurrentItem(newPos, true)
                }
            }
        })

        viewModel.skipNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val newPos = viewPager.currentItem + 2
                if (newPos == (viewPager.adapter?.itemCount ?: 0)) {
                    navigateToDone()
                } else {
                    viewPager.setCurrentItem(newPos, true)
                }
            }
        })

        viewModel.navigateToPrevPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                onBackPressed()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            (activity as? AppCompatActivity)?.loading(it, ProgressDialogFragment())
        })
    }

    private fun navigateToDone() {
        // if onboarding already complete, go directly to the main page
        if (viewModel.isOnboardingComplete) {
            goToMainActivity()
        } else {
            viewModel.completeOnboarding()

            val action =
                ViewPagerFragmentDirections.actionGlobalDoneFragment()
            findNavController().navigate(action)
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun onBackPressed() {
        val newPos = viewPager.currentItem - 1
        if (newPos >= 0) {
            viewPager.setCurrentItem(newPos, true)
        } else {
            activity?.finish()
        }
    }
}
