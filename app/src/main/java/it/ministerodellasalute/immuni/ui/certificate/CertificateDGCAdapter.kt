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
import android.text.format.DateFormat
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
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

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
        val daysExpiredDgcMap = settingsManager.settings.value.days_expiration_dgc
        holder.nameForename.text =
            "${greenCertificate.data?.person?.givenName} ${greenCertificate.data?.person?.familyName}"
        holder.addedInHome.visibility = if (greenCertificate.addedHomeDgc) {
            View.VISIBLE
        } else {
            View.GONE
        }
        when (true) {
            greenCertificate.data?.recoveryStatements != null -> {
                holder.dateEvent.text = when(greenCertificate.fglTipoDgc) {
                    "cbis" -> dateExpired(
                        greenCertificate.data?.recoveryStatements?.get(0)?.certificateValidFrom!!,
                        dateFormat,
                        daysExpiredDgcMap["cbis"]!!,
                        holder
                    )
                    else -> dateExpired(
                        greenCertificate.data?.recoveryStatements?.get(0)?.certificateValidFrom!!,
                        dateFormat,
                        daysExpiredDgcMap["healing_certificate"]!!,
                        holder
                    )
                }

                holder.eventType.text = context.getString(R.string.green_certificate_card_recovery)
            }
            greenCertificate.data?.tests != null -> {
                if (greenCertificate.data?.tests?.get(0)!!.typeOfTest == molecolarTest) {
                    holder.eventType.text =
                        context.getString(R.string.green_certificate_card_molecular_test)
                    holder.dateEvent.text = dateExpired(
                        greenCertificate.data.tests?.get(0)?.dateTimeOfTestResult!!,
                        dateTimeFormat,
                        daysExpiredDgcMap["molecular_test"]!!,
                        holder
                    )
                } else {
                    holder.eventType.text =
                        context.getString(R.string.green_certificate_card_rapid_test)
                    holder.dateEvent.text = dateExpired(
                        greenCertificate.data.tests?.get(0)?.dateTimeOfTestResult!!,
                        dateTimeFormat,
                        daysExpiredDgcMap["rapid_test"]!!,
                        holder
                    )
                }
            }
            greenCertificate.data?.vaccinations != null -> {
                holder.dateEvent.text = if (greenCertificate.data?.vaccinations?.get(0)!!.doseNumber < greenCertificate.data.vaccinations?.get(0)!!.totalSeriesOfDoses) {
                    dateExpired(
                        greenCertificate.data.vaccinations?.get(0)?.dateOfVaccination!!,
                        dateFormat,
                        daysExpiredDgcMap["vaccine_first_dose"]!!,
                        holder
                    )
                } else if (greenCertificate.data.vaccinations?.get(0)!!.doseNumber == greenCertificate.data.vaccinations?.get(0)!!.totalSeriesOfDoses && greenCertificate.data.vaccinations?.get(0)!!.totalSeriesOfDoses < 3) {
                    dateExpired(
                        greenCertificate.data.vaccinations?.get(0)?.dateOfVaccination!!,
                        dateFormat,
                        daysExpiredDgcMap["vaccine_fully_completed"]!!,
                        holder
                    )
                } else {
                    dateExpired(
                        greenCertificate.data.vaccinations?.get(0)?.dateOfVaccination!!,
                        dateFormat,
                        daysExpiredDgcMap["vaccine_booster"]!!,
                        holder
                    )
                }
                holder.eventType.text = context.getString(
                    R.string.green_certificate_card_vaccination,
                    greenCertificate.data.vaccinations?.get(0)?.doseNumber,
                    greenCertificate.data.vaccinations?.get(0)?.totalSeriesOfDoses
                )
            }
            greenCertificate.data?.exemptions != null -> {
                holder.dateEvent.text = if (greenCertificate.data?.exemptions!![0].certificateValidUntil != null) {
                    val todayDateMill = Date().byAdding().time
                    val maxDateValidity = dateFormat.parse(greenCertificate.data.exemptions!![0].certificateValidUntil!!)!!
                    if (maxDateValidity.time > todayDateMill) {
                        context.getString(R.string.green_certificate_list_dgc_valid, DateFormat.getDateFormat(context).format(greenCertificate.data.exemptions!![0].certificateValidUntil))
                    } else {
                        context.getString(R.string.green_certificate_list_dgc_expired)
                    }
                } else {
                    context.getString(R.string.green_certificate_list_dgc_expired)
                }
                holder.eventType.text = context.getString(R.string.green_certificate_card_exemption)
            }
        }
    }

    private fun dateExpired(dateFrom: String,simpleDateFormat: SimpleDateFormat, daysValidity: Int, holder: GreenPassVH): String {
        val todayDateMill = Date().byAdding().time
        val maxDateValidity = simpleDateFormat.parse(dateFrom)!!.byAdding(days = daysValidity)
        return if (maxDateValidity.time > todayDateMill) {
            holder.validityDGC.backgroundTintList = context.resources.getColorStateList(R.color.colorPrimary, null)
            context.getString(R.string.green_certificate_list_dgc_valid, DateFormat.getDateFormat(context).format(maxDateValidity))
        } else {
            holder.validityDGC.backgroundTintList = context.resources.getColorStateList(R.color.danger, null)
            context.getString(R.string.green_certificate_list_dgc_expired)
        }
    }
}

interface CertificateDGCClickListener {
    fun onClick(uid: String)
}
