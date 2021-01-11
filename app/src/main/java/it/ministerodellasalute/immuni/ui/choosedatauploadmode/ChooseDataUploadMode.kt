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

package it.ministerodellasalute.immuni.ui.choosedatauploadmode

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.DataUploadDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.visible
import kotlinx.android.synthetic.main.choose_data_upload_mode.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class ChooseDataUploadMode : Fragment(R.layout.choose_data_upload_mode) {

    private lateinit var viewModel: ChooseDataUploadModeViewModel

    companion object {
        var NAVIGATE_UP = false
    }

    override fun onResume() {
        super.onResume()
        if (NAVIGATE_UP) {
            NAVIGATE_UP = false
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background_darker))

        viewModel = getViewModel()

        navigationIcon.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        nextOS.setSafeOnClickListener {
            val action =
                DataUploadDirections.actionUploadData()
            findNavController().navigate(action)
        }

        nextIndependently.setSafeOnClickListener {
            val action = DataUploadDirections.actionReportPositivityIndependently()
            findNavController().navigate(action)
        }

        viewModel.allowedRegionsSelfUpload.observe(viewLifecycleOwner) {
            if (viewModel.allowedRegionsSelfUpload.value!!.contains(viewModel.region.value?.region)) {
                nextIndependently.isEnabled = true
                allowedRegionsSelfUploadError.gone()
            } else {
                nextIndependently.isEnabled = false
                allowedRegionsSelfUploadError.visible()
            }
        }
    }
}
