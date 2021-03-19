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

package it.ministerodellasalute.immuni.ui.upload

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.ui.cun.ReportPositivityIndependently
import it.ministerodellasalute.immuni.ui.otp.OtpFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UploadSuccessFragment : Fragment(R.layout.upload_data_success_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background))

        lifecycleScope.launch {
            delay(2000)
            close()
        }
    }

    private fun close() {
        val args = navArgs<UploadSuccessFragmentArgs>()
        if (args.value.callCenterMode) {
            OtpFragment.NAVIGATE_UP = true
        }
        if (args.value.navigateUpIndependently) {
            ReportPositivityIndependently.NAVIGATE_UP = true
        } else {
            OtpFragment.NAVIGATE_UP = true
        }
        activity?.finish()
    }
}
