package it.ministerodellasalute.immuni.ui.countriesofinterest

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.SettingsNavDirections
import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.settings.CountriesOfInterestManager
import it.ministerodellasalute.immuni.logic.settings.models.Country
import it.ministerodellasalute.immuni.logic.settings.models.CountryOb
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.PopupRecyclerViewDialogFragment
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import kotlinx.android.synthetic.main.settings_countries_of_interest.*
import org.koin.android.ext.android.inject

class CountriesPreferences : PopupRecyclerViewDialogFragment(), NationsClickListener,
    ConfirmationDialogListener {

    val ALERT_CONFIRM_DELETE = 210

    private val nationsManager: CountriesOfInterestManager by inject()
    lateinit var adapter: CountriesListAdapter
    private var itemClicked: ExposureIngestionService.Country? = null
    private var itemListSelected =
        nationsManager.getCountriesSelected()!!.listCountries.toMutableList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.settings_countries_of_interest)
//        setTitle(getString(R.string.title_nation_preferences))
        setTitle("Scegli i paesi di interesse")

        adapter = CountriesListAdapter(this)
        recyclerView.adapter = adapter
        saveButton.isEnabled = false
        adapter.data = nationsManager.getCountries()

        if (nationsManager.getCountriesSelected()!!.listCountries.isNotEmpty()) {
            for (country in adapter.data) {
                for (countrySelected in nationsManager.getCountriesSelected()!!.listCountries) {
                    if (country == countrySelected.country) {
                        adapter.selectedCountries!!.add(country)
                    }
                }
            }
            adapter.notifyDataSetChanged()
            validate()
        }

        knowMore.setSafeOnClickListener {
            val action = SettingsNavDirections.actionCountriesExplanation()
            findNavController().navigate(action)
        }

        saveButton.setOnClickListener(null)
        saveButton.setSafeOnClickListener {
            var listNationsToSave = CountryOb()
            for (country in adapter.selectedCountries!!) {
                listNationsToSave.listCountries.add(Country(country = country))
            }
            nationsManager.save(listNationsToSave)
            findNavController().popBackStack()
        }

    }

    override fun onDialogNegative(requestCode: Int) {
        // Do nothing, user does not want to exit
    }

    override fun onDialogPositive(requestCode: Int) {
        if (requestCode == ALERT_CONFIRM_DELETE) {
            adapter.selectedCountries!!.remove(itemClicked)
            itemListSelected.remove(Country(itemClicked))
        }
        adapter.notifyDataSetChanged()
        validate()
        itemClicked = null
    }

    private fun validate() {
        saveButton.isEnabled =
            nationsManager.getCountriesSelected()!!.listCountries.isNotEmpty() || !adapter.selectedCountries.isNullOrEmpty()
    }

    override fun onClick(item: ExposureIngestionService.Country) {
        itemClicked = item

        if (adapter.selectedCountries!!.contains(item) && itemListSelected.contains(Country(item))) {
            openConfirmationDialog(
                positiveButton = "Remove",
                negativeButton = getString(R.string.cancel),
                message = "Sei sicuro di voler rimuovere",
                title = "Title",
                cancelable = true,
                requestCode = ALERT_CONFIRM_DELETE
            )
        } else if (adapter.selectedCountries!!.contains(item)) {
            adapter.selectedCountries!!.remove(item)
        } else {
            adapter.selectedCountries!!.add(item)
        }
        adapter.notifyDataSetChanged()
        validate()
    }
}
