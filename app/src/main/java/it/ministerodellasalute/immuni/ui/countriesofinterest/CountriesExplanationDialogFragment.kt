package it.ministerodellasalute.immuni.ui.countriesofinterest

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment

class CountriesExplanationDialogFragment : PopupDialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.countries_explanation_dialog)

//        setTitle(getString(R.string.permission_tutorial_why_province_region_title))
        setTitle("A cosa serve scegliere il paese/i dove devi viaggiare?")
    }

}
