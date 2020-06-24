package it.ministerodellasalute.immuni.ui.onboarding.fragments.dialogs

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment

class RegionProvinceExplanationDialogFragment : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.region_province_explanation_dialog)

        setTitle(getString(R.string.permission_tutorial_why_province_region_title))
    }
}
