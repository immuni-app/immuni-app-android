package com.bendingspoons.ascolto.ui.onboarding

import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.findNavController
import com.bendingspoons.ascolto.AscoltoActivity
import com.bendingspoons.ascolto.R
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class OnboardingActivity : AscoltoActivity() {

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getStateViewModel()

        setContentView(R.layout.onboarding_activity)
        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.onboarding)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }
}
