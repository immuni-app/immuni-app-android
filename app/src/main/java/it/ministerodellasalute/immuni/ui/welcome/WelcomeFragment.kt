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

package it.ministerodellasalute.immuni.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBarFullscreen
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.ui.main.MainActivity
import kotlinx.android.synthetic.main.welcome_fragment.*
import org.koin.android.ext.android.inject

class WelcomeFragment : Fragment() {

    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    private val userManager: UserManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        if (userManager.isOnboardingComplete.value) {
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(R.color.statusBarLight))

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUI()
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
        }

        with(viewPager) {
            adapter = WelcomeAdapter(this@WelcomeFragment)
            clipToPadding = false
            clipChildren = false
            // isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Some implementation
        }.attach()

        knowMore.setSafeOnClickListener {
            val action = WelcomeFragmentDirections.actionHowitworks(false)
            findNavController().navigate(action)
        }

        next.setSafeOnClickListener(intervalMillis = 250) {
            val newPos = viewPager.currentItem + 1
            if (newPos == (viewPager.adapter?.itemCount ?: 0)) {
                userManager.setWelcomeComplete(true)
                navigateTo()
            } else {
                viewPager.setCurrentItem(newPos, true)
            }
        }
    }

    private fun updateUI() {
        val newPos = viewPager.currentItem + 1
        if (newPos == (viewPager.adapter?.itemCount ?: 0)) {
            next.text = getString(R.string.welcome_view_go_next)
        } else {
            next.text = getString(R.string.next)
        }
    }

    private fun navigateTo() {
        if (userManager.isOnboardingComplete.value) navigateToHome() else navigateToOnboarding()
    }

    private fun navigateToOnboarding() {
        val action = WelcomeFragmentDirections.actionOnboardingActivity(false)
        findNavController().navigate(action)
    }

    private fun navigateToHome() {
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
