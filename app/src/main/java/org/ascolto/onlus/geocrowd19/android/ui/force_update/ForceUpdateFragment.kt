package org.ascolto.onlus.geocrowd19.android.ui.force_update

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.ascolto.onlus.geocrowd19.android.R
import kotlinx.android.synthetic.main.force_update_fragment.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ForceUpdateFragment : Fragment(R.layout.force_update_fragment) {
    private lateinit var viewModel: ForceUpdateViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()

        update.setOnClickListener {
            context?.let { viewModel.goToPlayStoreAppDetails(it) }
        }
    }
}
