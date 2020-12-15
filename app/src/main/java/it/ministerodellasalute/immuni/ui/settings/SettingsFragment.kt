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

package it.ministerodellasalute.immuni.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.BuildConfig
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.SettingsNavDirections
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.playstore.PlayStoreActions
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import kotlin.math.abs
import kotlinx.android.synthetic.main.settings_fragment.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SettingsFragment : Fragment(R.layout.settings_fragment), ConfirmationDialogListener {

    private lateinit var viewModel: SettingsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background_darker))

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())

            pageTitle?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
            toolbarSeparator?.alpha = ratio
        })

        // data management

        dataLoadButton.setSafeOnClickListener {
            val action = SettingsNavDirections.actionDataUploadNav()
            findNavController().navigate(action)
        }

        // information

        faqButton.setSafeOnClickListener {
            viewModel.onFaqClick()
        }
        termsOfServiceButton.setSafeOnClickListener {
            viewModel.onTouClick(this)
        }
        privacyPolicyButton.setSafeOnClickListener {
            // viewModel.onPrivacyPolicyClick(this)
            val action = SettingsFragmentDirections.actionPrivacy()
            findNavController().navigate(action)
        }

        // general

        chooseCountriesOfInterestButton.setSafeOnClickListener {
            val action = SettingsFragmentDirections.actionCountriesOfInterest()
            findNavController().navigate(action)
        }

        changeProvinceButton.setSafeOnClickListener {
            val action = SettingsNavDirections.actionOnboardingActivity(true)
            findNavController().navigate(action)
        }
        sendFeedbackButton.setSafeOnClickListener {
            PlayStoreActions.goToPlayStoreAppDetails(requireContext(), requireContext().packageName)
        }
        contactSupport.setSafeOnClickListener {
            val action = SettingsNavDirections.actionSupport()
            findNavController().navigate(action)
        }
        shareApp.setSafeOnClickListener {
            ExternalLinksHelper.shareText(
                requireContext(),
                message = getString(R.string.settings_setting_share_message),
                subject = getString(R.string.app_name),
                chooserTitle = getString(R.string.settings_setting_share)
            )
        }

        applicationVersion.text = getString(
            R.string.settings_app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        viewModel.navigateToFaqs.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(SettingsNavDirections.actionFaq())
            }
        })

        viewModel.errorFetchingFaqs.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                openConfirmationDialog(
                    positiveButton = getString(android.R.string.ok),
                    message = getString(R.string.upload_data_connection_error_message),
                    title = getString(R.string.upload_data_connection_error_title),
                    cancelable = true,
                    requestCode = 0
                )
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            (activity as? AppCompatActivity)?.loading(it, ProgressDialogFragment())
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SettingsViewModel.EXPOSURE_NOTIFICATION_SETTINGS_REQUEST) {
            // Nothing to do
        }
    }

    override fun onDialogPositive(requestCode: Int) {}

    override fun onDialogNegative(requestCode: Int) {}
}
