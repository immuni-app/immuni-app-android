package it.ministerodellasalute.immuni.ui.support

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.coloredClickable
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import it.ministerodellasalute.immuni.util.startPhoneDial
import kotlinx.android.synthetic.main.support_dialog.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SupportDialogFragment : PopupDialogFragment() {

    private lateinit var viewModel: SupportViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel()

        setContentLayout(R.layout.support_dialog)
        setTitle(getString(R.string.support_title))

        viewModel.contactSupportPhone.observe(viewLifecycleOwner) {
            contactSupport.text = getString(R.string.support_contact_support)
                .coloredClickable(color = requireContext().getColorCompat(R.color.colorPrimary)) {
                    startPhoneDial(it)
                }
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
    }
}
