package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment

class KnowMoreGreenCertificate : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.know_more_green_certificate)

        setTitle(getString(R.string.green_pass_how_to_generate_title))
    }
}
