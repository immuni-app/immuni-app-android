package it.ministerodellasalute.immuni.ui.greencertificate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
    val fragment: GreenCertificateFragment
) :
    RecyclerView.Adapter<GreenPassAdapter.GreenPassVH>() {

    var data: List<GreenCertificateUser> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class GreenPassVH(v: View) : RecyclerView.ViewHolder(v) {
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

    override fun onBindViewHolder(holder: GreenPassVH, position: Int) {
        if (data.isNotEmpty()) {
            holder.qrCode.setImageBitmap(ImageUtils.convert(data[position].base64))
        }
        if (data.size > 1) {
            holder.swipeToShowQR.visibility = View.VISIBLE
        } else {
            holder.swipeToShowQR.visibility = View.GONE
        }

        holder.surnameNameText.text = data[position].nameSurname
        holder.birthDateText.text = data[position].birthDate
        holder.certificateIdText.text = data[position].certificateID

        holder.moreDetails.setSafeOnClickListener {
            val action = GreenCertificateDirections.actionMoreDetailsGreenCertificateDialog(data[position])
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
