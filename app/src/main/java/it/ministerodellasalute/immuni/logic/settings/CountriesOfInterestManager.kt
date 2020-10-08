package it.ministerodellasalute.immuni.logic.settings

import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsStoreRepository
import org.koin.core.KoinComponent

class CountriesOfInterestManager(
    private val exposureReportingRepository: ExposureReportingRepository,
    private val settingsRepository: ConfigurationSettingsStoreRepository
) : KoinComponent {

    fun setCountriesOfInterest(listCountries: List<CountryOfInterest>) {
        exposureReportingRepository.setCountriesOfInterest(listCountries)
    }

    fun getCountriesSelected(): List<CountryOfInterest> {
        return exposureReportingRepository.getCountriesOfInterest()
    }

    fun getCountries(): MutableList<CountryOfInterest> {
        val listCountries = mutableListOf<CountryOfInterest>()
        val countries = (settingsRepository.loadSettings()).countries
        for (country in countries) {
            listCountries.add(
                CountryOfInterest(
                    code = country.key,
                    fullName = country.value,
                    insertDate = null
                )
            )
        }
        return listCountries
    }

}
