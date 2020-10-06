package it.ministerodellasalute.immuni.logic.settings

import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
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

//    fun getCountries(): List<ExposureIngestionService.Country> =
//        ExposureIngestionService.Country.values().toList()

    fun getCountries(): MutableList<ExposureIngestionService.Country> {
        val listCountries = mutableListOf<ExposureIngestionService.Country>()
        val country = (settingsRepository.loadSettings()).countries
        for (coun in country) {
            listCountries.add(ExposureIngestionService.Country.fromCode(coun.key))
        }
        return listCountries
    }
}
