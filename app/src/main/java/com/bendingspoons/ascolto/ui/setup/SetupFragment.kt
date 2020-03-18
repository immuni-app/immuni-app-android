package com.bendingspoons.ascolto.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.AscoltoApplication
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.LogActivity
import com.bendingspoons.base.extensions.invisible
import com.bendingspoons.base.extensions.visible
import kotlinx.android.synthetic.main.setup_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class SetupFragment : Fragment(R.layout.setup_fragment) {

    companion object {
        fun newInstance() = SetupFragment()
    }

    private val viewModel: SetupViewModel by viewModel()

    override fun onResume() {
        super.onResume()

        viewModel.initializeApp()
    }

    override fun onPause() {
        super.onPause()

        viewModel.cancelInitializeJob()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error.invisible()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //startLogoAnimation()

        viewModel.navigateToMainPage.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { navigate ->// Only proceed if the event has never been handled
                if(navigate) {
                    goToLogActivity()
                }
            }
        })

        viewModel.navigateToOnboarding.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { navigate ->// Only proceed if the event has never been handled
                if(navigate) {
                    //goToOnboardingActivity()
                    goToLogActivity()
                }
            }
        })

        viewModel.errorDuringSetup.observe(viewLifecycleOwner, Observer { it ->
            if(it) error.visible()
            else error.invisible()
        })
    }

    private fun goToLogActivity() {
        val intent = Intent(AscoltoApplication.appContext, LogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun goToOnboardingActivity() {
        /*
        val intent = Intent(AscoltoApplication.appContext, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()

         */
    }
}
