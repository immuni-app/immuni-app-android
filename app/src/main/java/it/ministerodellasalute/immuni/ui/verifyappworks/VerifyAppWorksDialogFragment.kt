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

package it.ministerodellasalute.immuni.ui.verifyappworks

import android.os.Bundle
import android.view.View
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.visible
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.verify_app_works_dialog.*
import org.koin.android.ext.android.inject

class VerifyAppWorksDialogFragment : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.verify_app_works_dialog)

        setTitle(getString(R.string.permission_tutorial_verify_immuni_works_title))

        checkReopenReminder()
    }

    private fun checkReopenReminder() {
        val settingsManager: ConfigurationSettingsManager by inject()
        val reopenReminder = settingsManager.settings.value.reopenReminder

        if (reopenReminder) {
            fourth.visible()
            first.text = getString(R.string.verify_immuni_works_first)
            second.text = getString(R.string.verify_immuni_works_second)
            third.text = getString(R.string.verify_immuni_works_third)
            fourth.text = getString(R.string.verify_immuni_works_fourth)
        } else {
            fourth.gone()
            first.text = getString(R.string.permission_tutorial_verify_immuni_works_first)
            second.text = getString(R.string.permission_tutorial_verify_immuni_works_second)
            third.text = getString(R.string.permission_tutorial_verify_immuni_works_third)
        }
    }
}
