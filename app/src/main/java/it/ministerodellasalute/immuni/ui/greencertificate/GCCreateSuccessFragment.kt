package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GCCreateSuccessFragment : Fragment(R.layout.gcgeneratesuccess) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(2000)
            close()
        }
    }

    private fun close() {
        GenerateGreenCertificate.NAVIGATE_UP = true
        findNavController().popBackStack()
    }
}
