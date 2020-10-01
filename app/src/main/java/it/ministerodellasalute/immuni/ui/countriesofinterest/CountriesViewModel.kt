package it.ministerodellasalute.immuni.ui.settings

import androidx.lifecycle.ViewModel
import it.ministerodellasalute.immuni.logic.settings.CountriesOfInterestManager
import org.koin.core.KoinComponent

class CountriesViewModel(
    private val nationsManager: CountriesOfInterestManager
): ViewModel(), KoinComponent {
//    private val _nation = MutableStateFlow<MutableList<ExposureIngestionService.Nazioni>>(mutableListOf())
//    val nation: MutableStateFlow<MutableList<ExposureIngestionService.Nazioni>> = _nation

    val countries by lazy {
        nationsManager.getCountries()
    }

//    fun onNationSelected(nationSelected: ExposureIngestionService.Nazioni) {
//
//
//        if(_nation  .value.contains(nationSelected)) {
//            _nation.value.remove(nationSelected)
//        } else{
//            _nation.value.add(nationSelected)
//        }
//    }


}
