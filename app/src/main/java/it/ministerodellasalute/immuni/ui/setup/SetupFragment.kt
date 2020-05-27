/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.invisible
import it.ministerodellasalute.immuni.ui.main.MainActivity
import it.ministerodellasalute.immuni.ui.welcome.WelcomeActivity
import kotlinx.android.synthetic.main.setup_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetupFragment : Fragment(R.layout.setup_fragment) {

    companion object {
        fun newInstance() = SetupFragment()
    }

    private val viewModel: SetupViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        viewModel.initializeApp()
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelInitializationJob()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error.invisible()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.navigationDestination.observe(viewLifecycleOwner, Observer {
            // Only proceed if the event has never been handled
            it.getContentIfNotHandled()?.let { destination ->
                navigateToDestination(destination)
            }
        })
    }

    private fun navigateToDestination(destination: SetupViewModel.Destination) {
        val activityClass = when (destination) {
            SetupViewModel.Destination.Welcome -> WelcomeActivity::class.java
            SetupViewModel.Destination.Home -> MainActivity::class.java
        }
        val intent = Intent(requireContext(), activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }
}
