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

package it.ministerodellasalute.immuni.ui.privacy

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.privacy_content.*
import kotlinx.android.synthetic.main.privacy_dialog.*
import org.koin.android.ext.android.inject

class PrivacyDialogFragment : PopupDialogFragment() {

    private val settingsManager: ConfigurationSettingsManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.privacy_dialog)

        setTitle(getString(R.string.privacy_title))
        cardAge.gone()
        cardPrivacy.gone()
        tos.gone()

        completePrivacy.setSafeOnClickListener {
            findNavController().popBackStack()
            ExternalLinksHelper.openLink(
                requireContext(),
                settingsManager.privacyNoticeUrl
            )
        }
    }
}
