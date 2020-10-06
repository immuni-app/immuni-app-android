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

package it.ministerodellasalute.immuni.ui.countriesofinterest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener

class CountriesListAdapter(private val clickListener: CountriesPreferences) :
    RecyclerView.Adapter<CountriesListAdapter.CountriesVH>() {

    var selectedCountries: MutableList<ExposureIngestionService.Country>? = mutableListOf()

    var data: List<ExposureIngestionService.Country> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun onItemClick(pos: Int) {
        if (pos != RecyclerView.NO_POSITION) {
            clickListener.onClick(data[pos])
        }
    }

    inner class CountriesVH(v: View) : RecyclerView.ViewHolder(v) {
        val checkBox: AppCompatCheckBox = v.findViewById(R.id.checkboxNazioni)

        init {
            v.setSafeOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountriesVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.countries_of_interest_list_item, parent, false)
        return CountriesVH(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: CountriesVH, position: Int) {
        val dataItem = data[position]
//        holder.checkBox.isEnabled = false
        holder.checkBox.text = dataItem.fullName
        holder.checkBox.isChecked = false
        for (country in this.selectedCountries!!) {
            if (country == data[position]) {
                holder.checkBox.isChecked = true
            }
        }
    }
}

interface CountriesClickListener {
    fun onClick(item: ExposureIngestionService.Country)
}
