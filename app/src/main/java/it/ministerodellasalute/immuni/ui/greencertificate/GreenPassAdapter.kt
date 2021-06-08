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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.ministerodellasalute.immuni.GreenCertificateDirections
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.util.ImageUtils

class GreenPassAdapter(
    val context: Context,
    val fragment: GreenCertificateFragment,
    val viewModel: GreenCertificateViewModel
) :
    RecyclerView.Adapter<GreenPassAdapter.GreenPassVH>() {

    var data: List<GreenCertificateUser> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class GreenPassVH(v: View) : RecyclerView.ViewHolder(v) {
        val container: LinearLayout = v.findViewById(R.id.container)
        val qrCode: ImageView = v.findViewById(R.id.qrCode)
        val downloadButton: Button = v.findViewById(R.id.downloadButton)
        val deleteButton: Button = v.findViewById(R.id.deleteButton)

        val surnameNameText: TextView = v.findViewById(R.id.surnameNameText)
        val birthDateText: TextView = v.findViewById(R.id.birthDateText)
        val certificateIdText: TextView = v.findViewById(R.id.certificateIDText)
        val moreDetails: TextView = v.findViewById(R.id.moreDetails)
        val swipeToShowQR: TextView = v.findViewById(R.id.swipeToShowQR)
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreenPassVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.green_certificate_tab, parent, false)
        return GreenPassVH(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GreenPassVH, position: Int) {
        val greenCertificate = data[position]
        if (data.isNotEmpty()) {
            holder.qrCode.setImageBitmap(ImageUtils.convert(greenCertificate.base64))
        }
        if (data.size > 1) {
            holder.swipeToShowQR.visibility = View.VISIBLE
        } else {
            holder.swipeToShowQR.visibility = View.GONE
        }

        holder.surnameNameText.text =
            greenCertificate.data?.person?.familyName + " " + greenCertificate.data?.person?.givenName

        if (holder.surnameNameText.text.isBlank()) holder.surnameNameText.text = "----"
        holder.birthDateText.text = greenCertificate.data?.dateOfBirth ?: "---"
        if (holder.birthDateText.text.isBlank()) holder.birthDateText.text = "----"

        holder.certificateIdText.text = when (true) {
            greenCertificate.data?.recoveryStatements != null -> {
                greenCertificate.data!!.recoveryStatements!![0].certificateIdentifier
            }
            greenCertificate.data?.tests != null -> {
                greenCertificate.data?.tests!![0].certificateIdentifier
            }
            greenCertificate.data?.vaccinations != null -> {
                greenCertificate.data!!.vaccinations!![0].certificateIdentifier
            }
            else -> null
        }

        holder.moreDetails.setSafeOnClickListener {
            val action =
                GreenCertificateDirections.actionMoreDetailsGreenCertificateDialog(data[position])
            fragment.findNavController().navigate(action)
        }

        holder.downloadButton.setSafeOnClickListener {
            fragment.checkPermission(data[position].base64, "TestFileName")
        }

        holder.deleteButton.setSafeOnClickListener {
            fragment.positionToDelete = position
            fragment.openConfirmationDialog(
                positiveButton = context.getString(R.string.green_pass_confirm_button),
                negativeButton = context.getString(R.string.cancel),
                message = context.getString(R.string.message_modal_confirm_delete_dcg),
                title = context.getString(R.string.title_modal_confirm_delete_dcg),
                cancelable = true,
                requestCode = GreenCertificateFragment.DELETE_QR
            )
        }
    }
}
