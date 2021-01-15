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

package it.ministerodellasalute.immuni.ui.otp

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.DataUploadDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.visible
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import it.ministerodellasalute.immuni.util.startPhoneDial
import kotlin.math.abs
import kotlinx.android.synthetic.main.otp_fragment.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class OtpFragment : Fragment(R.layout.otp_fragment) {

    companion object {
        var NAVIGATE_UP = false
    }

    private lateinit var viewModel: OtpViewModel

    override fun onResume() {
        super.onResume()
        if (NAVIGATE_UP) {
            NAVIGATE_UP = false
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background_darker))
        // Warning: if you get the sharedViewModel, then every time you create this fragment from
        // the same activity, you will have same OTP code.
        viewModel = getViewModel()

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
        })

        val args = navArgs<OtpFragmentArgs>()
        if (args.value.callCenterMode) {
            toolbarTitle.text = getString(R.string.call_center_title)
            healthcareAuthorizationWarning.text = getString(R.string.call_center_description)
            setClickableCallCenterNumber()
        }

        navigationIcon.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        verify.setSafeOnClickListener { viewModel.verify() }

        knowMore.setSafeOnClickListener {
            val action = if (args.value.callCenterMode) {
                DataUploadDirections.actionHowToUploadPositiveCallCenter()
            } else {
                DataUploadDirections.actionHowToUploadPositive()
            }
            findNavController().navigate(action)
        }

        viewModel.otpCode.observe(viewLifecycleOwner) { otpCode.text = it }

        viewModel.loading.observe(viewLifecycleOwner) {
            activity?.loading(it, ProgressDialogFragment(), Bundle().apply {
                putString(
                    ProgressDialogFragment.MESSAGE,
                    getString(R.string.upload_data_verify_loading)
                )
            })
        }

        viewModel.verificationError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { text ->
                authorizationError.text = text
                authorizationError.visible()
            }
        }

        viewModel.buttonDisabledMessage.observe(viewLifecycleOwner) {
            if (it == null) {
                verify.text = getString(R.string.upload_data_verify_button)
                verify.isEnabled = true
                authorizationError.gone()
            } else {
                verify.text = it
                verify.isEnabled = false
            }
        }

        viewModel.navigateToUploadPage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { token ->
                val action = OtpFragmentDirections.actionUploadActivity(
                    OtpToken.fromLogic(token),
                    null,
                    args.value.callCenterMode,
                    args.value.callCenterMode
                )
                findNavController().navigate(action)

//                lifecycleScope.launch {
//                    delay(1000)
//                    findNavController().popBackStack()
//                }
            }
        }
    }

    private fun setClickableCallCenterNumber() {
        val spannableString = SpannableString(healthcareAuthorizationWarning.text)
        val numberCallCenter = getString(R.string.call_center_number)
        var startIndexOfLink = -1
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = requireContext().getColorCompat(R.color.colorPrimary)
                textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                val phone = numberCallCenter.replace(".", "")
                startPhoneDial(phone)
            }
        }
        startIndexOfLink = healthcareAuthorizationWarning.text.toString()
            .indexOf(numberCallCenter, startIndexOfLink + 1)
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + numberCallCenter.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        healthcareAuthorizationWarning.movementMethod =
            LinkMovementMethod.getInstance()
        healthcareAuthorizationWarning.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}
