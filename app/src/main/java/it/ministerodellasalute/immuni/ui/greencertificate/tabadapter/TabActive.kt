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

package it.ministerodellasalute.immuni.ui.greencertificate.tabadapter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.util.ImageUtils
import kotlinx.android.synthetic.main.green_certificate.*
import kotlinx.android.synthetic.main.green_certificate_active.*
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent

class TabActive : Fragment(R.layout.green_certificate_active), KoinComponent,
    ConfirmationDialogListener {

    private lateinit var userManager: UserManager
    private var fragmentParent: Fragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentParent = parentFragment
        userManager = get()

        setVisibilityQR()

        delete.setSafeOnClickListener {
            openConfirmationDialog(
                positiveButton = getString(R.string.green_pass_confirm_button),
                negativeButton = getString(R.string.cancel),
                message = getString(R.string.message_modal_confirm_delete_dcg),
                title = getString(R.string.title_modal_confirm_delete_dcg),
                cancelable = true,
                requestCode = 0
            )
        }
    }

    private fun setVisibilityQR() {
        if (userManager.user.value?.greenPass == null) {
            noQrCode.visibility = View.VISIBLE
            qrCodeLayout.visibility = View.GONE
        } else {
            qrCode.setImageBitmap(ImageUtils.convert(userManager.user.value?.greenPass!!.base64))
            noQrCode.visibility = View.GONE
            qrCodeLayout.visibility = View.VISIBLE
        }
    }

    override fun onDialogPositive(requestCode: Int) {
        val user = userManager.user
        userManager.save(
            User(
                region = user.value?.region!!,
                province = user.value?.province!!,
                greenPass = null
            )
        )
        fragmentParent?.tabLayout?.getTabAt(0)?.text = getString(R.string.green_pass_inactive)
        fragmentParent?.tabLayout?.tabTextColors =
            resources.getColorStateList(R.color.grey_dark, null)
        setVisibilityQR()
    }

    override fun onDialogNegative(requestCode: Int) {
        // Pass
    }
}
