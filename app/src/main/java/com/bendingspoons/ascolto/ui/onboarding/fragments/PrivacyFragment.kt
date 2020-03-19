package com.bendingspoons.ascolto.ui.onboarding.fragments

import android.graphics.Color
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
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import kotlinx.android.synthetic.main.onboarding_privacy_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class PrivacyFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.onboarding_privacy_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setDarkStatusBarFullscreen(resources.getColor(android.R.color.transparent))

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
            viewModel.onTosClick()
        }

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            updateUI()
        }

        back.setOnClickListener {
            activity?.finish()
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val action = PrivacyFragmentDirections.actionGlobalProfileFragment()
                findNavController().navigate(action)
            }
        })
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateUI()
    }

    private fun updateUI() {
        next.isEnabled = checkBox.isChecked
    }

}