package it.ministerodellasalute.immuni.ui.support

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment

class SupportDialogFragment : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.support_dialog)

        setTitle(getString(R.string.support_title))
    }
}
