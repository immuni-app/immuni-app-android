package it.ministerodellasalute.immuni.logic.settings

import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import org.koin.core.KoinComponent

class CountriesOfInterestManager(
    private val exposureReportingRepository: ExposureReportingRepository
) : KoinComponent {

    fun setCountriesOfInterest(listCountries: List<CountryOfInterest>) {
        exposureReportingRepository.setCountriesOfInterest(listCountries)
    }

    fun getCountriesSelected(): List<CountryOfInterest> {
        return exposureReportingRepository.getCountriesOfInterest()
    }

    fun getCountries(): List<ExposureIngestionService.Country> =
        ExposureIngestionService.Country.values().toList()
}
