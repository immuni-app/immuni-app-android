package it.ministerodellasalute.immuni.ui.certificate

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment

class CertificateDGCKnowMore: PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.certificate_dgc_know_more)

        setTitle(getString(R.string.certificate_dgc_know_more_title))
    }
}
