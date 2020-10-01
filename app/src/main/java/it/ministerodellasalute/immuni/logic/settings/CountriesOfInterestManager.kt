package it.ministerodellasalute.immuni.logic.settings

import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.logic.settings.models.CountryOb
import it.ministerodellasalute.immuni.logic.settings.repositories.CountriesOfInterestRepository
import org.koin.core.KoinComponent

class CountriesOfInterestManager(
    private val countriesOfInterestRepository: CountriesOfInterestRepository
) : KoinComponent {

    //val nations = nationsRepository.nations

    fun save(nations: CountryOb) {
        countriesOfInterestRepository.save(nations)
    }

    fun getCountriesSelected(): CountryOb? {
        return if (countriesOfInterestRepository.getCountriesSelected() == null) {
            return CountryOb()
        } else {
            countriesOfInterestRepository.getCountriesSelected()
        }
//        return countriesOfInterestRepository.getNationsSelected()
    }

    fun getCountries(): List<ExposureIngestionService.Country> = countriesOfInterestRepository.getCountries()
}
