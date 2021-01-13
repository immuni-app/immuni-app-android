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

package it.ministerodellasalute.immuni.ui.upload

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.loading
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.visible
import it.ministerodellasalute.immuni.ui.cun.CunToken
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.ui.otp.OtpToken
import it.ministerodellasalute.immuni.util.ProgressDialogFragment
import kotlin.math.abs
import kotlinx.android.synthetic.main.upload_data_fragment.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class UploadDataFragment : Fragment(R.layout.upload_data_fragment), ConfirmationDialogListener {

    private lateinit var viewModel: UploadViewModel
    private var token: OtpToken? = null
    private var cun: CunToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onDismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()

        val args = navArgs<UploadDataFragmentArgs>()
        if (args.value.token != null) {
            token = args.value.token
        } else {
            cun = args.value.cun
        }

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())

            pageTitle?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
            toolbarSeparator?.alpha = ratio
        })

        upload.setSafeOnClickListener {
            uploadOtp()
        }

        back.setSafeOnClickListener {
            onDismiss()
        }

        if (viewModel.hasExposureSummaries) {
            epidemiologicalData.visible()
        } else {
            epidemiologicalData.gone()
        }

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            (activity as? AppCompatActivity)?.loading(it, ProgressDialogFragment(), Bundle().apply {
                putString(ProgressDialogFragment.MESSAGE, getString(R.string.upload_data_send_data_loading))
            })
        })

        viewModel.uploadSuccess.observe(viewLifecycleOwner, Observer {
            val action = UploadDataFragmentDirections.actionGlobalSuccess(args.value.navigateUpIndependently, args.value.callCenterMode)
            findNavController().navigate(action)
        })

        viewModel.uploadError.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                openConfirmationDialog(
                    positiveButton = getString(R.string.retry),
                    negativeButton = getString(R.string.cancel),
                    message = getString(R.string.upload_data_vpn_error_message),
                    title = getString(R.string.upload_data_vpn_error_title),
                    cancelable = true,
                    requestCode = ALERT_REQUEST_ERROR
                )
            }
        })
    }

    private fun uploadOtp() {
        if (token != null) {
            viewModel.upload(requireActivity(), token!!.toLogic(), null)
        } else {
            viewModel.upload(requireActivity(), null, cun!!.toLogic())
        }
    }

    private fun onDismiss() {
        openConfirmationDialog(
            positiveButton = getString(R.string.confirm_data_close_verify_alert_affermative_answer),
            negativeButton = getString(R.string.cancel),
            message = getString(R.string.confirm_data_close_verify_alert_message),
            title = getString(R.string.confirm_data_close_verify_alert_title),
            cancelable = false,
            requestCode = ALERT_CONFIRM_EXIT
        )
    }

    override fun onDialogNegative(requestCode: Int) {
        // Do nothing, user does not want to exit
    }

    override fun onDialogPositive(requestCode: Int) {
        if (requestCode == ALERT_CONFIRM_EXIT) {
            activity?.finish()
        }
        if (requestCode == ALERT_REQUEST_ERROR) {
            uploadOtp()
        }
    }

    companion object {
        const val ALERT_CONFIRM_EXIT = 210
        const val ALERT_REQUEST_ERROR = 211
    }
}
