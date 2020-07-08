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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.models.Province

class ProvinceListAdapter(private val clickListener: ProvinceClickListener) :
    RecyclerView.Adapter<ProvinceListAdapter.ProvinceVH>() {

    var selectedProvince: Province? = null

    var data: List<Province> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun onItemClick(pos: Int) {
        if (pos != RecyclerView.NO_POSITION) {
            clickListener.onClick(data[pos])
        }
    }

    inner class ProvinceVH(v: View) : RecyclerView.ViewHolder(v) {
        val radioButton: RadioButton = v.findViewById(R.id.radioButton)

        init {
            v.setSafeOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinceVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.radio_list_item, parent, false)
        return ProvinceVH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ProvinceVH, position: Int) {
        val dataItem = data[position]
        holder.radioButton.isEnabled = false
        holder.radioButton.text = when (dataItem) {
            Province.abroad -> holder.itemView.context.getString(R.string.onboarding_region_abroad_item)
            else -> dataItem.fullName
        }

        holder.radioButton.isChecked = data[position] == selectedProvince
    }
}

interface ProvinceClickListener {
    fun onClick(item: Province)
}
