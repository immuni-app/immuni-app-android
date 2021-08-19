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

package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.ExternalLinksHelper
import it.ministerodellasalute.immuni.extensions.view.getColorCompat
import it.ministerodellasalute.immuni.ui.dialog.PopupDialogFragment
import kotlinx.android.synthetic.main.green_certificate_know_more.*

class KnowMoreGreenCertificate : PopupDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.green_certificate_know_more)

        setTitle(getString(R.string.green_pass_how_to_generate_title))

//        setClickableLink()
    }

    private fun setClickableLink() {
        val spannableString = SpannableString(getString(R.string.green_pass_how_to_generate_sixth))
        val linkDCG = getString(R.string.green_pass_how_to_generate_link)
        var startIndexOfLink = -1
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = requireContext().getColorCompat(R.color.colorPrimary)
                textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                val link = if (!linkDCG.contains("http")) {
                    "https://$linkDCG"
                } else {
                    linkDCG
                }
                ExternalLinksHelper.openLink(
                    requireContext(),
                    link
                )
            }
        }
        startIndexOfLink = green_pass_sixth.text.toString()
            .indexOf(linkDCG, startIndexOfLink + 1)
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + linkDCG.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        green_pass_sixth.movementMethod =
            LinkMovementMethod.getInstance()
        green_pass_sixth.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}
