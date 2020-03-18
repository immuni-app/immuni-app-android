package com.bendingspoons.ascolto.ui.log.fragment

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
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.toast
import com.bendingspoons.ascolto.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.form_fragment.*
import kotlinx.android.synthetic.main.form_fragment.progress
import kotlinx.android.synthetic.main.form_fragment.viewPager
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FormFragment : Fragment() {

    private lateinit var viewModel: LogViewModel
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
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.form_fragment, container, false)
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
            adapter = FormAdapter(this@FormFragment)
            clipToPadding = false
            clipChildren = false
            isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        close.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val newPos = viewPager.currentItem + 1
                if(newPos ==  (viewPager.adapter?.itemCount ?: 0)) {
                    //navigateToDone()
                    toast("Finish form, navigate next")
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