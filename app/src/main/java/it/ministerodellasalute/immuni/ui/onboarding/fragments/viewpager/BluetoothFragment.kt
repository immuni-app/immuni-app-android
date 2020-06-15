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

package it.ministerodellasalute.immuni.ui.onboarding.fragments.viewpager

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.bluetooth.BluetoothUtils
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.*

class BluetoothFragment :
    ViewPagerBaseFragment(R.layout.onboarding_bluetooth_fragment) {

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.setLightStatusBar(requireContext().getColorCompat(R.color.background))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        next.isEnabled = true

        next.setOnClickListener(null)
        next.setSafeOnClickListener {
            viewModel.onNextTap()
        }

        setupImage(R.raw.lottie_girls_06, R.drawable.ic_onboarding_bluetooth)
        checkSpacing()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothUtils.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                viewModel.onNextTap()
            }
        }
    }
}
