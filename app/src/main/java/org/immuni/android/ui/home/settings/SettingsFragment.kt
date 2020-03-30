package org.immuni.android.ui.home.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.immuni.android.R
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.log_choose_person_fragment.*
import kotlinx.android.synthetic.main.settings_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        faqButton.setOnClickListener {
            viewModel.onFaqClick()
        }

        privacyPolicyButton.setOnClickListener {
            viewModel.onPrivacyPolicyClick()
        }

        tosButton.setOnClickListener {
            viewModel.onTosClick()
        }
    }
}
