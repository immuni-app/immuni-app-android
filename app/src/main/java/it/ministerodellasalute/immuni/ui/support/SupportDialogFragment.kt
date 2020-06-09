package it.ministerodellasalute.immuni.ui.support

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.SettingsDirections
import it.ministerodellasalute.immuni.extensions.utils.coloredClickable
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import it.ministerodellasalute.immuni.util.startPhoneDial
import it.ministerodellasalute.immuni.util.startSendingEmail
import kotlinx.android.synthetic.main.support_dialog.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SupportDialogFragment : PopupDialogFragment() {

    private lateinit var viewModel: SupportViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel()

        setContentLayout(R.layout.support_dialog)
        setTitle(getString(R.string.support_title))

        contactSupport.movementMethod = LinkMovementMethod.getInstance()
        viewModel.contactSupportPhone.observe(viewLifecycleOwner) {

            if (it == null) phoneContainer.gone()
            else {
                @SuppressLint("SetTextI18n")
                contactSupport.text = "{$it}"
                    .coloredClickable(
                        color = requireContext().getColorCompat(R.color.colorPrimary),
                        bold = true
                    ) {
                        startPhoneDial(it)
                    }
            }
        }

        mailSupport.movementMethod = LinkMovementMethod.getInstance()
        viewModel.contactSupportEmail.observe(viewLifecycleOwner) {
            if (it == null) mailContainer.gone()
            else {
                @SuppressLint("SetTextI18n")
                mailSupport.text = "{$it}"
                    .coloredClickable(
                        color = requireContext().getColorCompat(R.color.colorPrimary),
                        bold = true
                    ) {
                        val message = "\n\n\n——————————\n" +
                            "${getString(R.string.support_email_body_message)}\n\n" +
                            listOf(
                                "${getString(R.string.support_info_item_os)}: ${viewModel.osVersion.value}",
                                "${getString(R.string.support_info_item_device)}: ${viewModel.deviceModel.value}",
                                "${getString(R.string.support_info_item_exposureNotificationEnabled)}: ${viewModel.isExposureNotificationEnabled.value}",
                                "${getString(R.string.support_info_item_bluetoothEnabled)}: ${viewModel.isBluetoothEnabled.value}",
                                "${getString(R.string.support_info_item_appVersion)}: ${viewModel.appVersion.value}",
                                "Google Play Services: ${viewModel.googlePlayVersion.value}",
                                "${getString(R.string.support_info_item_connectionType)}: ${viewModel.connectionType.value}"
                            ).joinToString(separator = "; ", postfix = ".")

                        startSendingEmail(it, subject = "", message = message)
                    }
            }
        }
        viewModel.supportWorkingHours.observe(viewLifecycleOwner) { (from, to) ->
            phoneDescription.text = getString(R.string.support_phone_description_android, from, to)
        }

        viewModel.osVersion.observe(viewLifecycleOwner) {
            osVersion.text = it
        }

        viewModel.deviceModel.observe(viewLifecycleOwner) {
            deviceModel.text = it
        }

        viewModel.isExposureNotificationEnabled.observe(viewLifecycleOwner) {
            isExposureNotificationEnabled.text = it
        }

        viewModel.isBluetoothEnabled.observe(viewLifecycleOwner) {
            isBluetoothActive.text = it
        }

        viewModel.appVersion.observe(viewLifecycleOwner) {
            appVersion.text = it
        }

        viewModel.googlePlayVersion.observe(viewLifecycleOwner) {
            playServicesVersion.text = it
        }

        viewModel.connectionType.observe(viewLifecycleOwner) {
            connectionType.text = it
        }

        openFaq.setSafeOnClickListener {
            dismiss()
            val action = SettingsDirections.actionFaq()
            findNavController().navigate(action)
        }
    }
}
