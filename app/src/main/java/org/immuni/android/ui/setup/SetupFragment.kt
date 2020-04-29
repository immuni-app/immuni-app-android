package org.immuni.android.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.home.HomeActivity
import org.immuni.android.ui.onboarding.OnboardingActivity
import org.immuni.android.ui.welcome.WelcomeActivity
import org.immuni.android.extensions.view.invisible
import org.immuni.android.extensions.view.visible
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
                    goToHomeActivity()
                }
            }
        })

        viewModel.navigateToOnboarding.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { navigate ->// Only proceed if the event has never been handled
                if(navigate) {
                    goToOnboardingActivity()
                }
            }
        })

        viewModel.navigateToWelcome.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { navigate ->// Only proceed if the event has never been handled
                if(navigate) {
                    goToWelcomeActivity()
                }
            }
        })

        viewModel.errorDuringSetup.observe(viewLifecycleOwner, Observer { it ->
            if(it) error.visible()
            else error.invisible()
        })
    }

    private fun goToHomeActivity() {
        val intent = Intent(ImmuniApplication.appContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun goToOnboardingActivity() {
        val intent = Intent(ImmuniApplication.appContext, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun goToWelcomeActivity() {
        val intent = Intent(ImmuniApplication.appContext, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }
}
