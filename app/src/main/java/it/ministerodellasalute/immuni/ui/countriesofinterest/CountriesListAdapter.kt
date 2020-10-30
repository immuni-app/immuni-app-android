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
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager.Companion.DAYS_OF_SELF_ISOLATION
import it.ministerodellasalute.immuni.extensions.utils.DateUtils.MILLIS_IN_A_DAY
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import java.util.*

class CountriesListAdapter(private val clickListener: CountriesFragment) :
    RecyclerView.Adapter<CountriesListAdapter.CountriesVH>() {

    var selectedCountries: MutableList<CountryOfInterest> = mutableListOf()

    var data: List<CountryOfInterest> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun onItemClick(pos: Int, checkBox: AppCompatCheckBox) {
        if (pos != RecyclerView.NO_POSITION && checkBox.isEnabled) {
            clickListener.onClick(data[pos])
        }
    }

    inner class CountriesVH(v: View) : RecyclerView.ViewHolder(v) {
        val checkBox: AppCompatCheckBox = v.findViewById(R.id.checkboxNazioni)

        init {
            v.setSafeOnClickListener {
                onItemClick(adapterPosition, checkBox)
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
        holder.checkBox.isEnabled = true
        holder.checkBox.isChecked = false
        holder.checkBox.text = data[position].fullName
        for (country in this.selectedCountries) {
            if (country.code == data[position].code && country.insertDate != null && Date() < Date(
                    country.insertDate!!.time + DAYS_OF_SELF_ISOLATION * MILLIS_IN_A_DAY
                )
            ) {
                holder.checkBox.isEnabled = false
                holder.checkBox.isChecked = true
                break
            } else if (country.code == data[position].code) {
                holder.checkBox.isEnabled = true
                holder.checkBox.isChecked = true
                break
            }
        }
    }
}

interface CountriesClickListener {
    fun onClick(item: CountryOfInterest)
}
