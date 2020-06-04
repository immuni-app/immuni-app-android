package it.ministerodellasalute.immuni.ui.disableservice

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.disable_exposure_api_dialog.*

class DisableExposureApiDialogFragment : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.disable_exposure_api_dialog)

        setTitle(getString(R.string.permission_tutorial_deactivate_service_title))

        disableExposureApi.setSafeOnClickListener {
            // TODO
        }
    }
}
