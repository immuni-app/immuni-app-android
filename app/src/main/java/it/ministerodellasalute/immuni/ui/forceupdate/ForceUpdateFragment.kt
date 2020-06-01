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

package it.ministerodellasalute.immuni.ui.forceupdate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import kotlinx.android.synthetic.main.force_update_fragment.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ForceUpdateFragment : Fragment(R.layout.force_update_fragment) {
    private lateinit var viewModel: ForceUpdateViewModel

    override fun onResume() {
        super.onResume()
        // if all good close this page
        if (!viewModel.updateRequired) {
            activity?.finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()

        when {
            viewModel.isAppOutdated -> {
                title.text = getString(R.string.force_update_view_app_title)
                message.text = getString(R.string.notifications_update_app_description)
                updateIcon.setImageResource(R.drawable.ic_update)
            }
            viewModel.playServicesRequireUpdate -> {
                title.text = getString(R.string.force_update_play_services_title)
                message.text = getString(R.string.force_update_play_services_message)
                updateIcon.setImageResource(R.drawable.ic_update)
            }
            viewModel.exposureNotificationsNotAvailable -> {
                title.text = getString(R.string.force_update_not_available_title)
                message.text = getString(R.string.force_update_not_available_message)
                update.visibility = View.INVISIBLE
                updateIcon.setImageResource(R.drawable.ic_update_wait)
            }
        }

        update.setSafeOnClickListener {
            startUpdate()
        }
    }

    private fun startUpdate() {
        if (viewModel.isAppOutdated) {
            viewModel.goToPlayStoreAppDetails(requireContext())
        } else if (viewModel.playServicesRequireUpdate) {
            viewModel.updatePlayServices(requireContext())
        }
    }
}
