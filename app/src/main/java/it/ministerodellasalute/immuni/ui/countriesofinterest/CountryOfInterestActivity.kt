package it.ministerodellasalute.immuni.ui.countriesofinterest

import android.os.Bundle
import androidx.navigation.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.ui.ImmuniActivity

class CountryOfInterestActivity : ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(resources.getColor(R.color.background))

        setContentView(R.layout.nav_host_activity)
        findNavController(R.id.nav_host_fragment).setGraph(
            R.navigation.countries_of_interest,
            intent.extras
        )
    }


}
