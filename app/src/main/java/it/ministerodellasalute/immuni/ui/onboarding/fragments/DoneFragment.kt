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

package it.ministerodellasalute.immuni.ui.onboarding.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.main.MainActivity
import it.ministerodellasalute.immuni.ui.onboarding.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class DoneFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // prevent crashes for invalid state
        if (savedInstanceState != null) activity?.finish()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // users must select a choice
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.onboarding_done_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToMainPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                goToMainActivity()
            }
        })

        viewModel.onEnterDonePage()
    }

    private fun goToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }
}
