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
    RecyclerView.Adapter<CountriesListAdapter.NationsVH>() {

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

    inner class NationsVH(v: View) : RecyclerView.ViewHolder(v) {
        val checkBox: AppCompatCheckBox = v.findViewById(R.id.checkboxNazioni)

        init {
            v.setSafeOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NationsVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.countries_of_interest_list_item, parent, false)
        return NationsVH(v)
    }

    override fun getItemCount(): Int{
        return data.size
    }
    override fun onBindViewHolder(holder: NationsVH, position: Int) {
        val dataItem = data[position]
        holder.checkBox.isEnabled = false
        holder.checkBox.text = dataItem.fullName

        //holder.checkBox.isChecked = selectedNations?.contains(dataItem)!!
        holder.checkBox.isChecked = false
        for(country in this.selectedCountries!!) {
            if(country == data[position]) {
                holder.checkBox.isChecked = true
            }
        }
    }
}

interface NationsClickListener {
    fun onClick(item: ExposureIngestionService.Country)
}
