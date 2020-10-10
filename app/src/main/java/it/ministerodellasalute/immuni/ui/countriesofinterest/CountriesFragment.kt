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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.exposure.CountriesOfInterestManager
import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import kotlinx.android.synthetic.main.countries_of_interest.*
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.math.abs

class CountriesFragment :
    Fragment(), CountriesClickListener, ConfirmationDialogListener {

    private val countriesManager: CountriesOfInterestManager by inject()
    lateinit var adapter: CountriesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.countries_of_interest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
            pageTitle?.alpha = 1 - ratio
            description?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
        }
        )

        adapter =
            CountriesListAdapter(
                this
            )
        recyclerView.adapter = adapter
        saveButton.isEnabled = false

        adapter.data = countriesManager.getCountries()

        if (countriesManager.getCountriesSelected().isNotEmpty()) {
            for (country in adapter.data) {
                for (countrySelected in countriesManager.getCountriesSelected()) {
                    if (country.code == countrySelected.code) {
                        adapter.selectedCountries.add(countrySelected)
                    }
                }
            }
            adapter.notifyDataSetChanged()
            validate()
        }

        saveButton.setOnClickListener(null)
        saveButton.setSafeOnClickListener {
            if (countriesManager.checkListsEqual(adapter.selectedCountries, countriesManager.getCountriesSelected())) {
                activity?.finish()
            } else {
                openConfirmationDialog(
                    positiveButton = getString(R.string.countries_of_interest_dialog_positive),
                    negativeButton = getString(R.string.countries_of_interest_dialog_negative),
                    message = getString(R.string.countries_of_interest_dialog_message),
                    title = getString(R.string.countries_of_interest_dialog_title),
                    cancelable = true,
                    requestCode = ALERT_CONFIRM_SAVE
                )
            }
        }
    }

    override fun onDialogNegative(requestCode: Int) {
        // Do nothing, user does not want to exit
    }

    override fun onDialogPositive(requestCode: Int) {
        if (requestCode == ALERT_CONFIRM_SAVE) {
            val listNationsToSave = countriesManager.getCountriesSelected()
            for (country in adapter.selectedCountries) {
                if (!listNationsToSave.contains(country)) {
                    listNationsToSave.add(
                        CountryOfInterest(
                            code = country.code,
                            fullName = country.fullName,
                            insertDate = Date()
                        )
                    )
                }
            }
            countriesManager.setCountriesOfInterest(listNationsToSave.toList())
            activity?.finish()
        }
    }

    private fun validate() {
        saveButton.isEnabled = countriesManager.getCountriesSelected()
            .isNotEmpty() || !adapter.selectedCountries.isNullOrEmpty()
    }

    override fun onClick(item: CountryOfInterest) {
        if (adapter.selectedCountries.contains(item)) {
            adapter.selectedCountries.remove(item)
        } else {
            adapter.selectedCountries.add(item)
        }
        adapter.notifyDataSetChanged()
        validate()
    }

    companion object {
        const val ALERT_CONFIRM_SAVE = 212
    }
}

