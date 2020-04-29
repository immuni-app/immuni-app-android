package org.immuni.android.ui.onboarding.fragments.profile

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
import org.immuni.android.R
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.immuni.android.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.onboarding_profile_fragment.*
import org.immuni.android.base.extensions.loading
import org.immuni.android.util.ProgressDialogFragment
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class ProfileFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.onboarding_profile_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        progress.clipToOutline = true
        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                progress.setStep(position + 1, viewPager.adapter?.itemCount?:1)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}
        }

        with(viewPager) {
            adapter = ProfileAdapter(this@ProfileFragment)
            clipToPadding = false
            clipChildren = false
            isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val newPos = viewPager.currentItem + 1
                if(newPos ==  (viewPager.adapter?.itemCount ?: 0)) {
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
        val action =
            ProfileFragmentDirections.actionGlobalDoneFragment()
        findNavController().navigate(action)
    }

    private fun onBackPressed() {
        val newPos = viewPager.currentItem - 1
        if(newPos >= 0) {
            viewPager.setCurrentItem(newPos, true)
        } else {
            activity?.finish()
        }
    }
}