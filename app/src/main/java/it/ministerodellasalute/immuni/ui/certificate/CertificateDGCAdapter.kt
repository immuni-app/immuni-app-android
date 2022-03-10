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

package it.ministerodellasalute.immuni.ui.certificate

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.utils.*
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.models.GreenCertificateUser
import java.text.SimpleDateFormat
import java.util.*

class CertificateDGCAdapter(
    val context: Context,
    val settingsManager: ConfigurationSettingsManager,
    private val clickListener: CertificateDGCClickListener
) :
    RecyclerView.Adapter<CertificateDGCAdapter.GreenPassVH>() {

    private val molecolarTest = "LP6464-4"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private val dateFormatString = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    var data: List<GreenCertificateUser> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun onItemClick(pos: Int) {
        if (pos != RecyclerView.NO_POSITION) {
            val uid = when (true) {
                data[pos].data?.recoveryStatements != null -> {
                    data[pos].data?.recoveryStatements?.get(0)?.certificateIdentifier
                }
                data[pos].data?.tests != null -> {
                    data[pos].data?.tests?.get(0)?.certificateIdentifier
                }
                data[pos].data?.vaccinations != null -> {
                    data[pos].data?.vaccinations?.get(0)?.certificateIdentifier
                }
                data[pos].data?.exemptions != null -> {
                    data[pos].data?.exemptions?.get(0)?.certificateIdentifier
                }
                else -> null
            }
            clickListener.onClick(uid!!)
        }
    }

    inner class GreenPassVH(v: View) : RecyclerView.ViewHolder(v) {
        val dateEvent: TextView = v.findViewById(R.id.dateEvent)
        val nameForename: TextView = v.findViewById(R.id.nameForename)
        val eventType: TextView = v.findViewById(R.id.eventType)
        val validityDGC: MaterialCardView = v.findViewById(R.id.validityDGC)
        val addedInHome: ImageView = v.findViewById(R.id.addedInHome)

        init {
            v.setSafeOnClickListener { onItemClick(adapterPosition) }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CertificateDGCAdapter.GreenPassVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.certificate_dgc_item, parent, false)
        return GreenPassVH(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GreenPassVH, position: Int) {
        val greenCertificate = data[position]
        val daysExpiredDgcMap = settingsManager.settings.value.eu_dcc_deadlines
        holder.nameForename.text =
            "${greenCertificate.data?.person?.familyName} ${greenCertificate.data?.person?.givenName}"
        holder.addedInHome.visibility = if (greenCertificate.addedHomeDgc) {
            View.VISIBLE
        } else {
            View.GONE
        }
        val todayDateMill = Date().byAdding().time
        when (true) {
            greenCertificate.data?.recoveryStatements != null -> {
                val validityDays = if (greenCertificate.fglTipoDgc == "cbis") {
                    daysExpiredDgcMap["cbis"]
                } else {
                    daysExpiredDgcMap["healing_certificate"]
                }
                val maxDateValidity = if (validityDays != null) {
                    dateFormat.parse(greenCertificate.data?.recoveryStatements?.get(0)?.certificateValidFrom!!)!!
                        .byAdding(days = validityDays)
                } else {
                    null
                }
                holder.dateEvent.text =
                    if (maxDateValidity !== null && maxDateValidity.time > todayDateMill) {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.colorPrimary, null)
                        context.getString(
                            R.string.green_certificate_list_dgc_valid,
                            dateFormatString.format(maxDateValidity)
                        )
                    } else {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.danger, null)
                        context.getString(R.string.green_certificate_list_dgc_expired)
                    }
                holder.eventType.text = context.getString(R.string.green_certificate_card_recovery)
            }
            greenCertificate.data?.tests != null -> {
                val validityHours =
                    if (greenCertificate.data?.tests?.get(0)!!.typeOfTest == molecolarTest) {
                        holder.eventType.text =
                            context.getString(R.string.green_certificate_card_molecular_test)
                        daysExpiredDgcMap["molecular_test"]
                    } else {
                        holder.eventType.text =
                            context.getString(R.string.green_certificate_card_rapid_test)
                        daysExpiredDgcMap["rapid_test"]
                    }
                val maxDateValidity = if (validityHours != null) {
                    dateTimeFormat.parse(greenCertificate.data.tests?.get(0)?.dateTimeOfCollection!!)!!
                        .byAdding(hours = validityHours)
                } else {
                    null
                }
                holder.dateEvent.text =
                    if (maxDateValidity !== null && maxDateValidity.time > todayDateMill) {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.colorPrimary, null)
                        if (greenCertificate.data.tests?.get(0)!!.typeOfTest == molecolarTest) {
                            settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["molecular_test"]
                        } else {
                            settingsManager.settings.value.eudcc_expiration[Locale.getDefault().language]!!["rapid_test"]
                        }
                    } else {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.danger, null)
                        context.getString(R.string.green_certificate_list_dgc_expired)
                    }
            }
            greenCertificate.data?.vaccinations != null -> {
                val validityDays =
                    if (greenCertificate.data?.vaccinations?.get(0)!!.doseNumber < greenCertificate.data.vaccinations?.get(
                            0
                        )!!.totalSeriesOfDoses
                    ) {
                        daysExpiredDgcMap["vaccine_first_dose"]
                    } else if (greenCertificate.data.vaccinations?.get(0)!!.doseNumber == greenCertificate.data.vaccinations?.get(
                            0
                        )!!.totalSeriesOfDoses && greenCertificate.data.vaccinations?.get(0)!!.totalSeriesOfDoses < 3
                    ) {
                        daysExpiredDgcMap["vaccine_fully_completed"]
                    } else {
                        daysExpiredDgcMap["vaccine_booster"]
                    }
                val maxDateValidity = if (validityDays != null) {
                    dateFormat.parse(greenCertificate.data.vaccinations?.get(0)?.dateOfVaccination!!)!!
                        .byAdding(days = validityDays)
                } else {
                    null
                }
                holder.dateEvent.text =
                    if (maxDateValidity !== null && maxDateValidity.time > todayDateMill) {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.colorPrimary, null)
                        context.getString(
                            R.string.green_certificate_list_dgc_valid,
                            dateFormatString.format(maxDateValidity)
                        )
                    } else {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.danger, null)
                        context.getString(R.string.green_certificate_list_dgc_expired)
                    }
                holder.eventType.text = context.getString(
                    R.string.green_certificate_card_vaccination,
                    greenCertificate.data.vaccinations?.get(0)?.doseNumber,
                    greenCertificate.data.vaccinations?.get(0)?.totalSeriesOfDoses
                )
            }
            greenCertificate.data?.exemptions != null -> {
                val maxDateValidity =
                    if (!greenCertificate.data?.exemptions!![0].certificateValidUntil.isNullOrBlank()) {
                        dateFormat.parse(greenCertificate.data.exemptions?.get(0)?.certificateValidUntil!!)!!
                    } else if (daysExpiredDgcMap["exemption"] != null) {
                        dateFormat.parse(greenCertificate.data.exemptions?.get(0)?.certificateValidFrom!!)!!
                            .byAdding(days = daysExpiredDgcMap["exemption"]!!)
                    } else {
                        null
                    }

                holder.dateEvent.text =
                    if (maxDateValidity != null && maxDateValidity.time > todayDateMill) {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.colorPrimary, null)
                        context.getString(
                            R.string.green_certificate_list_dgc_valid,
                            dateFormatString.format(maxDateValidity)
                        )
                    } else {
                        holder.validityDGC.backgroundTintList =
                            context.resources.getColorStateList(R.color.danger, null)
                        context.getString(R.string.green_certificate_list_dgc_expired)
                    }
                holder.eventType.text = context.getString(R.string.green_certificate_card_exemption)
            }
        }
    }
}

interface CertificateDGCClickListener {
    fun onClick(uid: String)
}
