package org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment.profile

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
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.add_relative_profile_fragment.*
import kotlinx.android.synthetic.main.onboarding_profile_fragment.*
import kotlinx.android.synthetic.main.onboarding_profile_fragment.progress
import kotlinx.android.synthetic.main.onboarding_profile_fragment.viewPager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ascolto.onlus.geocrowd19.android.loading
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.AddRelativeViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class RelativeProfileFragment : Fragment() {

    private lateinit var viewModel: AddRelativeViewModel
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    private val MAX_PAGE = 7
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.add_relative_profile_fragment, container, false)
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
                progress.setStep(position + 1, MAX_PAGE)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}
        }

        with(viewPager) {
            adapter = RelativeAdapter(this@RelativeProfileFragment)
            clipToPadding = false
            clipChildren = false
            isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { fragmentClass ->
                (viewPager.adapter as RelativeAdapter).apply {
                    addPage(fragmentClass)
                    notifyDataSetChanged()
                }
                val newPos = viewPager.currentItem + 1
                viewPager.setCurrentItem(newPos, true)
            }
        })

        viewModel.navigateToPrevPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                onBackPressed()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it)
        })

        close.setOnClickListener {
            activity?.finish()
        }
    }

    private fun navigateToDone() {
        val action =
            RelativeProfileFragmentDirections.actionInterrupt("titolo mu", "descrizione mu")
        findNavController().navigate(action)
    }

    private fun onBackPressed() {
        val newPos = viewPager.currentItem - 1
        if(newPos >= 0) {
            // do nothing
        } else {
            activity?.finish()
        }
    }
}