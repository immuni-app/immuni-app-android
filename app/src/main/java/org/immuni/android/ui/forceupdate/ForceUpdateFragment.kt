package org.immuni.android.ui.forceupdate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.immuni.android.R
import kotlinx.android.synthetic.main.force_update_fragment.*
import org.immuni.android.loading
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ForceUpdateFragment : Fragment(R.layout.force_update_fragment) {
    private lateinit var viewModel: ForceUpdateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // deny back press to force update
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it)
        })

        viewModel.downloading.observe(viewLifecycleOwner, Observer {
            title.text = "Download in corso..."
            message.text = "Controlla l'avanzamento del download nella barra delle notifiche."
            update.isEnabled = false
            update.alpha = 0.3f
        })

        update.setOnClickListener {
            context?.let { viewModel.goToPlayStoreAppDetails(requireActivity()) }
        }
    }

    override fun onResume() {
        super.onResume()
        title.text = getString(R.string.app_update_title)
        message.text = getString(R.string.app_update_message)
        update.isEnabled = true
        update.alpha = 1f
    }
}
