package com.bendingspoons.ascolto.ui.welcome

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
import androidx.viewpager2.widget.ViewPager2
import com.bendingspoons.ascolto.AscoltoApplication
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.onboarding.OnboardingActivity
import com.bendingspoons.ascolto.ui.onboarding.OnboardingViewModel
import com.bendingspoons.ascolto.ui.onboarding.fragments.WelcomeFragmentDirections
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.onboarding_profile_fragment.*
import kotlinx.android.synthetic.main.welcome_fragment.*
import kotlinx.android.synthetic.main.welcome_fragment.viewPager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class WelcomeFragment : Fragment() {

    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackPressed()
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
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUI()
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}
        }

        with(viewPager) {
            adapter = WelcomeAdapter(this@WelcomeFragment)
            clipToPadding = false
            clipChildren = false
            //isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        next.setOnClickListener {
            val newPos = viewPager.currentItem + 1
            if(newPos ==  (viewPager.adapter?.itemCount ?: 0)) {
                navigateToOnboarding()
                val welcome: Welcome by inject()
                welcome.setCompleted(true)
            } else {
                viewPager.setCurrentItem(newPos, true)
            }
        }
    }

    private fun updateUI() {
        val newPos = viewPager.currentItem + 1
        if(newPos ==  (viewPager.adapter?.itemCount ?: 0)) {
            next.text = getString(R.string.welcome_lets_start)
        } else {
            next.text = getString(R.string.next)
        }
    }

    private fun navigateToOnboarding() {
        val intent = Intent(AscoltoApplication.appContext, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun onBackPressed() {
        val newPos = viewPager.currentItem - 1
        if(newPos >= 0) {
            viewPager.setCurrentItem(newPos, true)
        } else {
            findNavController().popBackStack()
        }
    }
}