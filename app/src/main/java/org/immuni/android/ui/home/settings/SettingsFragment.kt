package org.immuni.android.ui.home.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.immuni.android.R
import org.immuni.android.ui.home.HomeSharedViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.settings_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        tosButton.setOnClickListener {
            viewModel.onTosClick()
        }

        dataHandlingButton.setOnClickListener {
            val action = SettingsFragmentDirections.actionGlobalDataHandling()
            findNavController().navigate(action)
        }
    }
}
