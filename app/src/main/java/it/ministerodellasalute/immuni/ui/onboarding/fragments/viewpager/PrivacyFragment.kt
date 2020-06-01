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

package it.ministerodellasalute.immuni.ui.onboarding.fragments.viewpager

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.extensions.utils.coloredClickable
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import kotlin.math.abs
import kotlinx.android.synthetic.main.onboarding_privacy_fragment.*
import kotlinx.android.synthetic.main.privacy_content.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class PrivacyFragment : ViewPagerBaseFragment(R.layout.onboarding_privacy_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getSharedViewModel()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background))
        updateUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())

            title?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
            toolbarSeparator?.alpha = ratio
        })

        next.setSafeOnClickListener {
            if (checkBoxPrivacy.isChecked && !checkBoxAge.isChecked) {
                scrollView.fullScroll(View.FOCUS_DOWN)
                appBar.setExpanded(false)
            } else if (checkBoxPrivacy.isChecked) {
                viewModel.onPrivacyPolicyAccepted()
            } else {
                scrollView.fullScroll(View.FOCUS_DOWN)
                appBar.setExpanded(false)
            }
            updateUI(true)
        }

        val privacyRawText = getString(R.string.privacy_checkbox_read)
        privacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        privacyPolicy.text = privacyRawText.coloredClickable(
            color = requireContext().getColorCompat(R.color.colorPrimary),
            bold = false
        ) {
            viewModel.onPrivacyPolicyClick(this)
        }

        val tosRawText = getString(R.string.privacy_tos_read)
        tos.movementMethod = LinkMovementMethod.getInstance()
        tos.text = tosRawText.coloredClickable(
            color = requireContext().getColorCompat(R.color.colorPrimary),
            bold = true
        ) {
            viewModel.onTosClick(this)
        }

        checkBoxPrivacy.setOnCheckedChangeListener { buttonView, isChecked ->
            highlight(cardPrivacy, false)
            updateUI()
        }

        checkBoxAge.setOnCheckedChangeListener { buttonView, isChecked ->
            highlight(cardAge, false)
            updateUI()
        }

        cardAge.setOnClickListener {
            checkBoxAge.isChecked = !checkBoxAge.isChecked
        }

        cardPrivacy.setOnClickListener {
            checkBoxPrivacy.isChecked = !checkBoxPrivacy.isChecked
        }

        navigationIcon.setSafeOnClickListener {
            activity?.finish()
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
            }
        })

        next.isEnabled = false
    }

    private fun updateUI(validation: Boolean = false) {
        // keep the button enabled, but change color (using state list color) when privacy
        // is not checked
        next.isEnabled = true

        // uncomment this is we want the button to change color
        // when it is not yet possible to proceed
        // next.isActivated = !checkBoxPrivacy.isChecked

        if (validation) {
            highlight(cardPrivacy, !checkBoxPrivacy.isChecked)
            highlight(cardAge, !checkBoxAge.isChecked)
        }
    }

    private fun highlight(card: MaterialCardView, highlight: Boolean) {
        card.strokeWidth = ScreenUtils.convertDpToPixels(requireContext(), 2)
        if (highlight) card.strokeColor = resources.getColor(R.color.danger)
        else card.strokeWidth = 0
        card.isActivated = highlight
    }
}
