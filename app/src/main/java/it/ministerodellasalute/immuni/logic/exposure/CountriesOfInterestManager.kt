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

package it.ministerodellasalute.immuni.logic.exposure

import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsStoreRepository
import java.util.*
import kotlin.collections.HashSet
import org.koin.core.KoinComponent

class CountriesOfInterestManager(
    private val exposureReportingRepository: ExposureReportingRepository,
    private val settingsRepository: ConfigurationSettingsStoreRepository
) : KoinComponent {

    fun selector(country: CountryOfInterest): String = country.fullName

    fun setCountriesOfInterest(listCountries: List<CountryOfInterest>) {
        exposureReportingRepository.setCountriesOfInterest(listCountries)
    }

    fun getCountriesSelected(): MutableList<CountryOfInterest> {
        return exposureReportingRepository.getCountriesOfInterest().toMutableList()
    }

    fun getCountries(): MutableList<CountryOfInterest> {
        val listCountries = mutableListOf<CountryOfInterest>()
        val settingsCountries = (settingsRepository.loadSettings()).countries
        val countries = settingsCountries.getOrDefault(Locale.getDefault().language, settingsCountries["en"])!!
        for (country in countries) {
            listCountries.add(
                CountryOfInterest(
                    code = country.key,
                    fullName = country.value,
                    insertDate = null
                )
            )
        }
        listCountries.sortBy { selector(it) }
        return listCountries
    }

    fun <T> checkListsEqual(
        list1: List<T>?,
        list2: List<T>?
    ): Boolean {
        return HashSet(list1) == HashSet(list2)
    }

    fun addRemoveFromListByItem(listCountries: MutableList<CountryOfInterest>, country: CountryOfInterest): MutableList<CountryOfInterest> {
        for (countryOfInterest in listCountries) {
            if (countryOfInterest.code == country.code) {
                listCountries.remove(countryOfInterest)
                return listCountries
            }
        }
        listCountries.add(country)
        return listCountries
    }
}
