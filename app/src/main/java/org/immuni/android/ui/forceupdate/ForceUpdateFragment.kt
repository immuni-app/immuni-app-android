package org.immuni.android.ui.forceupdate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import org.immuni.android.R
import kotlinx.android.synthetic.main.force_update_fragment.*
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

        update.setOnClickListener {
            context?.let { viewModel.goToPlayStoreAppDetails(it) }
        }
    }
}
