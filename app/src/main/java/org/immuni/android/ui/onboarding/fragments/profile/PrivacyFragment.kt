package org.immuni.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.immuni.android.R
import org.immuni.android.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.onboarding_privacy_fragment.*
import kotlinx.android.synthetic.main.onboarding_privacy_fragment.back
import kotlinx.android.synthetic.main.onboarding_privacy_fragment.next
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class PrivacyFragment : ProfileContentFragment(R.layout.onboarding_privacy_fragment) {
    override val nextButton: View
        get() = next

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getSharedViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        next.setOnClickListener {
            viewModel.onPrivacyPolicyAccepted()
        }

        val text = getString(R.string.privacy_checkbox)
        val textWithoutPlaceholders = text.replace("{", "").replace("}", "")
        val start = text.indexOf("{")
        val end = text.indexOf("}")
        val spannable = SpannableString(textWithoutPlaceholders)
        spannable.setSpan(ForegroundColorSpan(
            resources.getColor(R.color.colorPrimary)),
            start,
            end - 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tos.text = spannable;

        tos.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            updateUI()
        }

        back.setOnClickListener {
            activity?.finish()
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {

            }
        })
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {}

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateUI()
    }

    private fun updateUI() {
        next.isEnabled = checkBox.isChecked
    }
}