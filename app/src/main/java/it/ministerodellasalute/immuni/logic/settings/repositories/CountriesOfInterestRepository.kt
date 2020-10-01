package it.ministerodellasalute.immuni.logic.settings.repositories

import it.ministerodellasalute.immuni.api.services.ExposureIngestionService
import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.logic.settings.models.CountryOb

class CountriesOfInterestRepository(
    private val storage: KVStorage
) {
    companion object {
        private val nationsKey = KVStorage.Key<CountryOb>("Country")
    }

    var nations = storage.stateFlow(nationsKey)

    fun save(listCountry: CountryOb) {
        storage[nationsKey] = listCountry
    }

    fun getCountriesSelected(): CountryOb? {
        return storage[nationsKey]
    }

    fun getCountries(): List<ExposureIngestionService.Country> =
        ExposureIngestionService.Country.values().toList()

}
