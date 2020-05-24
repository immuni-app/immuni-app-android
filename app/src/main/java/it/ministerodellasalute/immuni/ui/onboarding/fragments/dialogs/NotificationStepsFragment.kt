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

package it.ministerodellasalute.immuni.ui.onboarding.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.notifications.PushNotificationManager
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.notification_steps_dialog.*
import org.koin.android.ext.android.inject

class NotificationStepsFragment : PopupDialogFragment() {

    private val pushNotificationManager by inject<PushNotificationManager>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.notification_steps_dialog)
        setToolbarColor(R.color.background_light)

        notificationStepsContainer.updateLayoutParams {
            height = (ScreenUtils.getScreenHeight(requireContext()) * 0.9f).toInt()
        }
        openSettings.setSafeOnClickListener {
            findNavController().popBackStack()
            pushNotificationManager.openNotificationsSettings(requireContext(), this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Auto-Dismiss view if user has enabled notifications
        if (pushNotificationManager.areNotificationsEnabled()) {
            dismiss()
        }
    }
}
